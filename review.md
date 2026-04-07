# TrackRig — Code Review

Detaljan pregled projekta sa osvrtom na best practices, dizajn patterne, sigurnost, bazu podataka i tehnologije.

---

## ✅ Šta je urađeno dobro

- **Java 21 (LTS)** — Moderna, podržana verzija. Odličan odabir.
- **Spring Boot 3.5.x** — Najnovija generacija frameworka.
- **Slojevita arhitektura** — Controller → Service → Repository jasno razdvojen. Entiteti ne izlaze iz servisnog sloja.
- **DTO pattern** — Sve request/response klase su odvojeni DTO-ovi sa factory `from()` metodama. Čisto i konzistentno.
- **Interface + Implementation za servise** — `WorkstationService` / `WorkstationServiceImpl` pattern je Spring best practice.
- **Constructor injection** — Koristi se umjesto `@Autowired` na polju. Ispravno i testabilno.
- **GlobalExceptionHandler** — RFC 7807 `ProblemDetail` format, nikad se ne izlaže stack trace korisniku.
- **Database integrity** — FK constraints, `ON DELETE SET NULL`, `ON DELETE CASCADE`, trigger za zaštitu komponenti, UNIQUE constraint na grid koordinatama.
- **SQL View za maintenance** — Logika `DUE_SOON` / `OVERDUE` je u bazi, a ne u Java kodu. Dobar pristup.
- **RBAC** — `@PreAuthorize` na servisnom sloju (ne controlleru) je ispravno mjesto.
- **BCrypt** za lozinke.
- **`ddl-auto=validate`** — Hibernate ne mijenja šemu automatski. Sigurno za produkciju.
- **Environment varijable** za sve osjetljive podatke (DB credentials, JWT secret).
- **Lombok** za redukciju boilerplate koda.
- **Partial success** na Excel importu — vraća importovane redove i greške po redu. Korisno.
- **`@Transactional`** na svim servisnim metodama.

---

## 🔴 Kritično — treba ispraviti

### 1. Nema testova
Jedini test je `TrackRigApplicationTests` koji samo provjerava da se context učita. Nema unit testova za servise, nema integracionih testova za kontrolere. Ovo je najveći nedostatak projekta.

**Šta dodati:**
- Unit testovi za `ComponentService`, `MaintenanceService`, `WorkstationService` (Mockito)
- Integration testovi za Auth i Component endpointe (`@SpringBootTest` + `MockMvc`)
- Test za `ImportService` sa validnim i nevalidnim Excel fajlovima
- Test za `GlobalExceptionHandler` (provjeri da se 409, 403, 404 ispravno vraćaju)

---

### 2. CORS je previše permisivan
```java
// SecurityConfig.java — trenutno:
corsConfiguration.setAllowedOriginPatterns(List.of("*"));
corsConfiguration.setAllowedMethods(List.of("*"));
corsConfiguration.setAllowedHeaders(List.of("*"));
```
U produkciji ovo znači da **bilo koji sajt** može slati requeste prema API-ju. Mora biti ograničeno na konkretne domene frontend aplikacije.

**Ispravka:**
```java
corsConfiguration.setAllowedOrigins(List.of("https://tvoj-frontend.com"));
corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
```

---

### 3. `/api/auth/register` je otvoren za sve
Svako može registrovati nalog. U poslovnom kontekstu (gaming centar), samo `OWNER` bi trebao moći kreirati naloge za zaposlenike. Trenutno svi novi korisnici automatski dobijaju `EMPLOYEE` ulogu, ali registracija nije zaštićena.

**Rješenje:** Dodati `/api/users` endpoint pod `OWNER` rolom za kreiranje korisnika, a `/api/auth/register` ili ukloniti ili zaštititi.

---

### 4. Nema Refresh Token mehanizma
JWT token istekne, a jedina opcija je novi login. Standardna praksa je implementacija `refresh token`-a (duži vijek trajanja, čuva se u bazi ili cache-u, rotira se pri svakom korišćenju).

---

## 🟡 Važno — preporučeno poboljšanje

### 5. Nema logovanja (SLF4J / Logback)
Nema ni jednog `log.info()`, `log.error()`, `log.warn()` u kodu. U produkciji to znači nula vidljivosti u šta sistem radi.

**Dodati logovanje u:**
- Svaki servis (info za operacije, warn za business logic greške)
- `JwtAuthenticationFilter` (debug level za neuspjele autentifikacije)
- `GlobalExceptionHandler` (log.error za 500-ke sa stack trace-om)
- `ImportService` (info koliko je redova importovano)

---

### 6. Nema paginacije na list endpointima
`GET /api/components` i `GET /api/workstations` vraćaju sve redove iz baze. Sa stotinama ili hiljadama komponenti ovo će postati problem.

**Rješenje:** Koristiti `Pageable` iz Spring Data:
```java
Page<Component> findAll(Pageable pageable);
// GET /api/components?page=0&size=20&sort=createdAt,desc
```

---

### 7. `@Transactional(readOnly = true)` nije korišten
Sve GET metode u servisima imaju samo `@Transactional`, bez `readOnly = true`. Ovo ima performansni uticaj jer Hibernate ne aktivira određene optimizacije za read-only kontekst.

**Ispravka:** Dodati `readOnly = true` na sve metode koje samo čitaju podatke:
```java
@Transactional(readOnly = true)
public List<WorkstationResponse> getAllWorkstations() { ... }
```

---

### 8. Nema OpenAPI / Swagger dokumentacije
`API_ENDPOINTS.md` postoji ali nije automatski generisan. Sa `springdoc-openapi` bibliotekom dokumentacija se generiše iz koda i uvijek je ažurna.

**Dodati u `pom.xml`:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.7.0</version>
</dependency>
```
Dostupno na `/swagger-ui.html`.

---

### 9. JJWT biblioteka je zastarjela verzija
Koristi se `jjwt` verzija `0.11.5`. Trenutna stabilna verzija je `0.12.x` koja ima poboljšan API i sigurnosne zakrpe.

**Ažurirati na:** `0.12.6` i prilagoditi API pozive (npr. `Jwts.parserBuilder()` → `Jwts.parser()`).

---

### 10. Nema indeksa na foreign key kolonama u bazi
PostgreSQL automatski kreira indekse za `PRIMARY KEY` i `UNIQUE` constraint-e, ali **ne** za foreign key kolone. Za tablice koje se često joinuju, nedostatak indeksa znači full scan.

**Dodati indekse:**
```sql
CREATE INDEX idx_component_workstation_id ON component(workstation_id);
CREATE INDEX idx_component_category_id    ON component(category_id);
CREATE INDEX idx_maintenance_log_workstation_id ON maintenance_log(workstation_id);
CREATE INDEX idx_maintenance_log_type_id  ON maintenance_log(maintenance_type_id);
```

---

### 11. SQL View koristi `CROSS JOIN` — potencijalni performansni problem
`view_maintenance_status` radi CROSS JOIN između `workstation` i `maintenance_type`. Ako centar ima 100 stanica i 20 tipova servisiranja, to je 2000 redova po svakom upitu. Sa rastom podataka ovo može biti sporo.

**Razmatranje:** Za veće instalacije razmisliti o materialized view ili cache sloju (Redis/Caffeine) za dashboard endpoint.

---

### 12. Nema validacije jačine lozinke
`RegisterRequest` samo provjerava da lozinka nije prazna (`@NotBlank`). Nema provjere minimalne dužine, broja, specijalnog znaka itd.

**Dodati:**
```java
@Size(min = 8, message = "Password must be at least 8 characters")
@Pattern(regexp = ".*[0-9].*", message = "Password must contain a number")
private String password;
```

---

## 🟢 Sitni detalji

### 13. `SimpleController` treba ukloniti
Postoji `SimpleController` sa test endpointom koji je vjerovatno ostavljen od razvoja. Treba ga ukloniti prije deploy-a.

---

### 14. Nema Spring Actuator-a
`spring-boot-actuator` pruža `/actuator/health` endpoint koji je koristan za monitoring, Docker healthcheck i load balancer konfiguraciju. Lako za dodati:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

### 15. Nema API versioniranja
Svi endpointi su pod `/api/...` bez verzije. Kada dođe do breaking promjena, migracija klijenata postaje problem.

**Preporučeno:** `/api/v1/workstations`, `/api/v1/components`, itd.

---

### 16. `PDF import` endpoint postoji ali je upitne korisnosti
`POST /api/components/import/pdf` je definisan, ali parsiranje tabela iz PDF-a je notorno nepouzdano. Excel import je pouzdano rješenje. Ako PDF import nije u aktivnoj upotrebi, bolje ga ukloniti ili jasno dokumentovati ograničenja.

---

### 17. Nema soft delete mehanizma
Brisanje komponenti i radnih stanica je permanentno. Za audit trail i istorijat (ko je šta obrisao i kada) preporučuje se soft delete pattern:
```java
@Column(name = "deleted_at")
private LocalDateTime deletedAt;
```

---

## Baza podataka — ocjena

Šema je solidno dizajnirana:

| Aspekt | Ocjena |
|---|---|
| Normalizacija | ✅ — Sve lookup tablice su odvojene (status, kategorija, tip održavanja) |
| Integritet podataka | ✅ — FK constraints, NOT NULL, UNIQUE na business ključevima |
| Trigger logika | ✅ — `trg_restrict_component_delete` ispravno štiti instaliranu komponentu |
| SQL View | ✅ — Maintenance status se računa u bazi, a ne u kodu |
| Indeksi | ⚠️ — Nedostaju indeksi na FK kolonama (vidi tačku 10) |
| Audit trail | ⚠️ — `created_at` postoji, ali nema `updated_at` ni `deleted_at` |
| Seed data | ✅ — SQL fajl sadrži korisne početne podatke |
| Šema verzionisanje | ❌ — Nema Flyway/Liquibase migracija |

**Preporuka za bazu:** Dodati Flyway za verzionisanje šeme:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```
SQL fajlovi bi se čuvali kao `V1__init.sql`, `V2__add_indexes.sql` itd. i automatski primjenjivali pri pokretanju. Ovo je standardna praksa za produkcione sisteme.

---

## Prioritetna lista popravki

| Prioritet | Stavka |
|---|---|
| 🔴 Kritično | Dodati testove (unit + integracija) |
| 🔴 Kritično | Restriktivna CORS konfiguracija za produkciju |
| 🔴 Kritično | Zaštititi ili ukloniti `/api/auth/register` |
| 🟡 Važno | Dodati SLF4J logovanje u servise i filtere |
| 🟡 Važno | `@Transactional(readOnly = true)` na GET metodama |
| 🟡 Važno | Paginacija na list endpointima |
| 🟡 Važno | Ažurirati JJWT na `0.12.x` |
| 🟡 Važno | Dodati FK indekse u bazi |
| 🟡 Važno | OpenAPI / Swagger dokumentacija |
| 🟡 Važno | Validacija jačine lozinke |
| 🟢 Preporučeno | Flyway migracije za šemu baze |
| 🟢 Preporučeno | Refresh token mehanizam |
| 🟢 Preporučeno | Spring Actuator health endpoint |
| 🟢 Preporučeno | API versionisanje (`/api/v1/`) |
| 🟢 Preporučeno | Soft delete za komponente i radne stanice |
| 🟢 Sitno | Ukloniti `SimpleController` |
| 🟢 Sitno | Razmotriti uklanjanje PDF import endpointa |
