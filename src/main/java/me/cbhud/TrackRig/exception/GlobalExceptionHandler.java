package me.cbhud.TrackRig.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ========================
    // 404 NOT FOUND
    // ========================

    // Triggered by ResourceNotFoundException thrown in service layer
    // (e.g. workstation/component/maintenanceType not found by ID)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create("trackrig/errors/not-found"));
        return problem;
    }

    // Triggered by Spring MVC when a static resource path doesn't exist
    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create("trackrig/errors/not-found"));
        return problem;
    }

    // ========================
    // 400 BAD REQUEST
    // ========================

    // Triggered when @Valid fails on a @RequestBody (e.g. missing required fields,
    // @NotBlank, @NotNull, @Size violations).
    // Returns a clean list of field-level errors like:
    // ["name: Workstation name is required", "statusId: must not be null"]
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setTitle("Validation Error");
        problem.setType(URI.create("trackrig/errors/validation"));
        problem.setProperty("errors", errors);
        return problem;
    }

    // ========================
    // 401 UNAUTHORIZED
    // ========================

    // Triggered by CustomUserDetailsServiceImpl.login() when email/password don't
    // match.
    // Without this handler, BadCredentialsException falls through to the generic
    // 500 handler.
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        problem.setTitle("Authentication Failed");
        problem.setType(URI.create("trackrig/errors/unauthorized"));
        return problem;
    }

    // ========================
    // 403 FORBIDDEN
    // ========================

    // Triggered by @PreAuthorize failures (e.g. EMPLOYEE tries to delete a
    // component).
    // Returns a clean 403 — without this, AccessDeniedException falls through to
    // the generic 500 handler.
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
        problem.setTitle("Access Denied");
        problem.setType(URI.create("trackrig/errors/access-denied"));
        return problem;
    }

    // ========================
    // 409 CONFLICT
    // ========================

    // Triggered by DuplicateResourceException thrown in service layer
    // (e.g. registering with an email that is already taken).
    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicateResource(DuplicateResourceException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setTitle("Duplicate Resource");
        problem.setType(URI.create("trackrig/errors/conflict"));
        return problem;
    }

    // Triggered by JPA/Hibernate when a DB-level constraint is violated.
    // We inspect the root cause message to return a specific, user-friendly error
    // for each known constraint in the schema. Falls back to a generic message
    // for any constraint not explicitly recognised.
    //
    // Constraints covered:
    // • component.serial_number → UNIQUE + NOT NULL
    // • maintenance_type.name → uq_maintenance_type_name UNIQUE
    // • component_category.name → UNIQUE
    // • app_user.email → UNIQUE (DB-level fallback if duplicate slips through)
    // • component assigned to workstation → trg_restrict_component_delete trigger
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDatabaseConstraint(DataIntegrityViolationException e) {
        String rootMessage = "";
        if (e.getCause() != null && e.getCause().getCause() != null) {
            rootMessage = e.getCause().getCause().getMessage();
        } else if (e.getCause() != null) {
            rootMessage = e.getCause().getMessage();
        }

        String detail = resolveConstraintMessage(rootMessage);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
        problem.setTitle("Data Integrity Violation");
        problem.setType(URI.create("trackrig/errors/integrity-violation"));
        return problem;
    }

    /**
     * Maps known PostgreSQL constraint/trigger messages to clean user-facing
     * messages.
     * Add new cases here whenever a new UNIQUE or TRIGGER constraint is added to
     * the schema.
     */
    private String resolveConstraintMessage(String rootMessage) {
        if (rootMessage == null) {
            return "A database constraint was violated.";
        }

        // component.serial_number UNIQUE violation
        if (rootMessage.contains("serial_number")) {
            return "A component with this serial number already exists. Serial numbers must be unique.";
        }

        // component.serial_number NOT NULL violation
        if (rootMessage.contains("serial_number") && rootMessage.contains("null")) {
            return "Serial number is required and cannot be empty.";
        }

        // uq_maintenance_type_name — maintenance_type.name UNIQUE violation
        if (rootMessage.contains("uq_maintenance_type_name")
                || rootMessage.contains("maintenance_type") && rootMessage.contains("name")) {
            return "A maintenance type with this name already exists. Names must be unique.";
        }

        // component_category.name UNIQUE violation
        if (rootMessage.contains("component_category") && rootMessage.contains("name")) {
            return "A component category with this name already exists. Names must be unique.";
        }

        // app_user.email UNIQUE violation (DB-level fallback, normally caught earlier
        // in service)
        if (rootMessage.contains("app_user") && rootMessage.contains("email")) {
            return "An account with this email already exists.";
        }

        // SQL trigger: trg_restrict_component_delete blocks deleting an assigned
        // component
        if (rootMessage.contains("Cannot delete component")) {
            return "Cannot delete this component because it is currently installed in a workstation. Move it to storage first.";
        }

        // workstation.grid_x, grid_y UNIQUE violation
        if (rootMessage.contains("uq_workstation_grid") ||
           (rootMessage.contains("workstation") && rootMessage.contains("grid_x") && rootMessage.contains("grid_y"))) {
            return "A workstation already exists at this grid position. Coordinates must be unique.";
        }

        // Generic fallback for any other constraint not explicitly mapped
        return "A database constraint was violated. Please check your input and try again.";
    }

    // ========================
    // 500 INTERNAL SERVER ERROR
    // ========================

    // Last-resort catch-all. Logs a generic message — never exposes internal
    // details.
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("trackrig/errors/internal"));
        return problem;
    }
}