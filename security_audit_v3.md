# TrackRig — Security Audit v3

**Scope**: Full audit of auth, JWT, self-management (`/users/me`), admin management (`/users/{id}`), and all supporting code.  
**Date**: 2026-04-21

---

## 1 — Security Logic Bugs

### 1.1 MANAGER can escalate any user (including themselves) to OWNER

| | |
|---|---|
| **Severity** | **HIGH — security logic bug** |
| **Location** | [AppUserController.java:64-71](file:///C:/Users/cbhud/Documents/GitHub/TrackRig/src/main/java/me/cbhud/TrackRig/controller/AppUserController.java#L64-L71) + [AppUserServiceImpl.java:143](file:///C:/Users/cbhud/Documents/GitHub/TrackRig/src/main/java/me/cbhud/TrackRig/service/AppUserServiceImpl.java#L143) |

`PATCH /users/{id}` is gated with `@PreAuthorize("hasAnyRole('MANAGER', 'OWNER')")`. The service then does:

```java
user.setRole(request.role());  // line 143 — accepts any Role enum value
```

A MANAGER can send `{"role": "OWNER"}` for any user ID — including their own — and become OWNER. This is a **privilege escalation**.

**Fix**: Either:
- (a) Restrict the `PATCH /users/{id}` endpoint to `OWNER` only, or
- (b) Add a check in the service: if the caller is MANAGER, reject role changes to `OWNER`. Pass the caller's `SecurityUser` into the service to enforce this.

---

### 1.2 `AdminUpdateUserRequest.role` is `@NotNull` — it always overwrites the role

| | |
|---|---|
| **Severity** | **MEDIUM — data integrity bug** |
| **Location** | [AdminUpdateUserRequest.java:19-20](file:///C:/Users/cbhud/Documents/GitHub/TrackRig/src/main/java/me/cbhud/TrackRig/dto/AdminUpdateUserRequest.java#L19-L20) + [AppUserServiceImpl.java:143](file:///C:/Users/cbhud/Documents/GitHub/TrackRig/src/main/java/me/cbhud/TrackRig/service/AppUserServiceImpl.java#L143) |

`username`, `email`, and `fullName` are nullable (only applied if provided). But `role` is `@NotNull` and always set unconditionally:

```java
// These are conditional — null means "don't change"
if (request.username() != null && ...) { ... }
if (request.email() != null && ...) { ... }
if (request.fullName() != null && ...) { ... }

// This is unconditional — always overwrites
user.setRole(request.role());
```

This means every admin PATCH **must** include `role`, even if the intent is just to change a fullName. If the caller forgets `role` in the JSON, validation fails. If they include it, they might accidentally change a role.

This is inconsistent with the partial-update semantics of the other fields.

**Fix**: Make `role` nullable and conditional like the other fields:
```java
if (request.role() != null) {
    user.setRole(request.role());
}
```
Remove `@NotNull`. Or, create a separate `PATCH /users/{id}/role` endpoint with `UpdateUserRoleRequest` (which you already have as a DTO but don't use anywhere).

---

### 1.3 `UserNotFoundException` has no handler — returns 500

| | |
|---|---|
| **Severity** | **MEDIUM — design issue** |
| **Location** | [GlobalExceptionHandler.java](file:///C:/Users/cbhud/Documents/GitHub/TrackRig/src/main/java/me/cbhud/TrackRig/exception/GlobalExceptionHandler.java) (missing handler) |

`AppUserServiceImpl` throws `UserNotFoundException` from `getUserById`, `updateUserById`, and `deleteUserById`. But `GlobalExceptionHandler` has no `@ExceptionHandler(UserNotFoundException.class)`. It falls through to the generic `Exception` handler and returns a **500** with `"Something went wrong"` instead of a **404**.

**Fix**:
```java
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<ApiErrorResponse> handleUserNotFound(UserNotFoundException exc) {
    ApiErrorResponse response = new ApiErrorResponse(
            HttpStatus.NOT_FOUND.name(),
            exc.getMessage(),
            LocalDateTime.now()
    );
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
}
```

---

## 2 — Design Issues

### 2.1 Admin can delete or mutate their own account via `/users/{id}`

| | |
|---|---|
| **Severity** | **LOW — design issue** |
| **Location** | [AppUserController.java:73-78](file:///C:/Users/cbhud/Documents/GitHub/TrackRig/src/main/java/me/cbhud/TrackRig/controller/AppUserController.java#L73-L78) |

Nothing prevents a MANAGER/OWNER from calling `DELETE /users/{id}` or `PATCH /users/{id}` with their own ID. An OWNER could demote themselves, or delete themselves, which could leave the system with no OWNER.

**Fix**: Add a guard in the service:
```java
if (id.equals(caller.getId())) {
    throw new IllegalArgumentException("Cannot modify your own account via admin endpoints. Use /users/me.");
}
```

---

### 2.2 `JwtService` is injected into `AppUserServiceImpl` but unused

| | |
|---|---|
| **Severity** | **LOW — dead dependency** |
| **Location** | [AppUserServiceImpl.java:8,21,25](file:///C:/Users/cbhud/Documents/GitHub/TrackRig/src/main/java/me/cbhud/TrackRig/service/AppUserServiceImpl.java#L8) |

Wait — actually it **is** used: `jwtService.generateToken(savedUser.getUsername())` on line 71 for the username-change flow. Disregard — this is correct.

---

### 2.3 `UsernameNotFoundException` from self-management endpoints returns 500

| | |
|---|---|
| **Severity** | **LOW — design issue** |
| **Location** | [AppUserServiceImpl.java:35,44,84,98](file:///C:/Users/cbhud/Documents/GitHub/TrackRig/src/main/java/me/cbhud/TrackRig/service/AppUserServiceImpl.java#L35) |

The self-management methods (`getCurrentUser`, `updateMyProfile`, `changeMyPassword`, `deleteMyAccount`) throw `UsernameNotFoundException` if the user isn't found. This extends `AuthenticationException`, so depending on how Spring Security's exception handling interacts with `@RestControllerAdvice`, it may produce inconsistent responses.

In practice, this path should be unreachable for authenticated users (the JWT filter already verified the user exists). But for defensive coding, consider adding a handler or simply using `UserNotFoundException` with a 404 handler.

**Fix**: Either (a) add a `@ExceptionHandler(UsernameNotFoundException.class)` returning 404, or (b) throw `UserNotFoundException` instead, once you add that handler per §1.3.

---

### 2.4 `UpdateUserRoleRequest` DTO exists but is unused

| | |
|---|---|
| **Severity** | **TRIVIAL — dead code** |
| **Location** | [UpdateUserRoleRequest.java](file:///C:/Users/cbhud/Documents/GitHub/TrackRig/src/main/java/me/cbhud/TrackRig/dto/UpdateUserRoleRequest.java) |

This DTO is defined but never referenced by any controller or service. Either use it for a dedicated `PATCH /users/{id}/role` endpoint, or delete it.

---

## 3 — Auth / JWT / Filter — No Issues

Everything previously audited remains clean:

| Area | Status |
|---|---|
| Register flow | ✅ `@Valid` → check-then-act + DB constraint → BCrypt → clean DTO response |
| Login flow | ✅ `AuthenticationManager.authenticate()` → JWT with `sub` only → DTO response |
| Password hashing | ✅ BCrypt default (strength 10). Password complexity enforced via `@Pattern` on `RegisterRequest` and `ChangePasswordRequest` |
| JWT generation | ✅ Minimal claims (`sub`, `iat`, `exp`). HMAC-SHA256 with 256-bit key |
| JWT validation | ✅ Signature verified, expiry checked, username matched |
| JWT filter | ✅ `OncePerRequestFilter`, skips `/auth/**`, loads user from DB, SLF4J logging |
| SecurityFilterChain | ✅ CSRF off (stateless), stateless sessions, `/auth/**` public, everything else authenticated |
| `SecurityUser` adapter | ✅ Clean decoupling of JPA entity from Spring Security principal |
| `.env` / `.gitignore` | ✅ `.env` gitignored, secrets referenced via `${...}` placeholders |
| Exception handling | ✅ Structured `ApiErrorResponse`, bad credentials, access denied, validation, method not allowed, generic catch-all |
| DTO/entity separation | ✅ All responses use record DTOs with `from()` factory methods. No entity serialized directly. |
| `ddl-auto=validate` | ✅ Production-safe schema management |
| `@Transactional` placement | ✅ On implementation methods, not interface |

---

## 4 — Optional Hardening (Not Urgent)

| Item | Context |
|---|---|
| **Rate limiting on `/auth/login`** | Acceptable without if behind an API gateway. Add `bucket4j` or similar if exposed directly. |
| **Self-delete should invalidate token** | After `DELETE /users/me`, the JWT remains valid for up to 1 hour. The filter will fail on the DB lookup, so it won't authenticate — but the user gets a 401 instead of a clear "account deleted" message. Acceptable for now. |
| **Pagination on `GET /users`** | `findAll()` loads every user into memory. Fine for small teams; add `Pageable` before you have hundreds of users. |
| **Audit logging on admin actions** | Admin delete/update operations aren't logged. Consider adding `log.info("Admin {} changed role of user {} to {}", ...)` for accountability. |
| **`changeMyPassword` — reject same password** | Currently allows setting the new password to the same as the current password. Add a check: `if (passwordEncoder.matches(request.newPassword(), user.getPassword()))`. |

---

## 5 — Priority Fix List

| Priority | Issue | Effort |
|---|---|---|
| **P0** | §1.1 — MANAGER can escalate to OWNER | 15 min |
| **P1** | §1.3 — Add `UserNotFoundException` handler (returns 404) | 5 min |
| **P1** | §1.2 — Make `role` nullable/conditional on admin PATCH, or separate the endpoint | 10 min |
| **P2** | §2.1 — Block admin from deleting/mutating themselves via `/users/{id}` | 10 min |
| **P2** | §2.3 — Handle `UsernameNotFoundException` consistently | 5 min |
| **P3** | §2.4 — Delete or use `UpdateUserRoleRequest` | 1 min |

---

## 6 — What's Already Good

- **Self-management architecture is well-designed**: `/users/me` for self-ops, `/users/{id}` for admin-ops. Clean separation.
- **Username change re-issues JWT**: Smart handling in `updateMyProfile` — if username changes, a new token is generated and returned via `UpdateMyProfileResponse`.
- **Password change requires current password**: `changeMyPassword` validates the current password before allowing the change. Correct.
- **Clean partial-update pattern**: `updateMyProfile` and `updateUserById` both do null-checks and only update provided fields (except the `role` issue in §1.2).
- **Proper `@Transactional` on all mutation methods**: Every write method in `AppUserServiceImpl` has `@Transactional`.
- **Password complexity regex**: Both `RegisterRequest` and `ChangePasswordRequest` enforce uppercase, lowercase, digit, and symbol via matching `@Pattern`. Consistent.
- **`AdminUserResponse` vs `UserResponse` split**: Admin sees `id`, `role`, `createdAt`. Regular user sees only their own profile fields. Good least-exposure design.
