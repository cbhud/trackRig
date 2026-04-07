# TrackRig — Project Overview

## Šta je TrackRig?

TrackRig je backend sistem za upravljanje inventarom i praćenje održavanja hardvera, namijenjen gaming centrima, LAN kafićima i esports prostorima. Omogućava centralizovano praćenje radnih stanica, komponenti i rasporeda servisiranja — sa jasnim korisničkim ulogama i vizuelnom mapom prostora.

---

## Tehnički stack

| Sloj | Tehnologija |
|---|---|
| Jezik | Java 21 (LTS) |
| Framework | Spring Boot 3.5.x |
| ORM | Hibernate / Spring Data JPA |
| Baza podataka | PostgreSQL |
| Sigurnost | Spring Security + JWT (JJWT 0.11.5) |
| Izvještaji | Apache POI (Excel), OpenPDF (PDF) |
| Build alat | Maven |
| Boilerplate | Lombok |
| Konfiguracija | spring-dotenv (.env fajl) |

---

## Arhitektura

Projekat prati standardnu Spring Boot **slojevitu arhitekturu**:

```
Controller → Service (Interface + Impl) → Repository → Entity
                          ↕
                         DTO (Request / Response)
```

Svaki sloj ima jasno definisanu odgovornost i međusobno komunicira isključivo kroz DTO objekte — entiteti nikad ne izlaze van servisnog sloja.

Ukupno: **7 controllera**, **11 servisa**, **9 repozitorija**, **9 entiteta**, **12 DTO klasa**, **3 custom exception-a + GlobalExceptionHandler**.

---

## Ključne funkcionalnosti

**Upravljanje radnim stanicama** — CRUD + praćenje statusa i pozicije na mapi (grid_x, grid_y).

**Inventar komponenti** — Praćenje hardvera (GPU, CPU, RAM, periferije) sa serijskim brojevima, kategorijama i statusima. Komponente koje nisu u radnoj stanici automatski se smatraju "u skladištu".

**Planiranje i logovanje održavanja** — Definisanje tipova servisiranja (npr. "zamjena termalne paste" svakih 180 dana). SQL view `view_maintenance_status` automatski izračunava status: `OK`, `DUE_SOON`, `OVERDUE` ili `NEVER_DONE`.

**Vizuelna mapa prostora** — 2D grid prikaz rasporeda radnih stanica na osnovu koordinata iz baze.

**Import / Export** — Masovni import komponenti iz Excel fajlova, export inventara i logova u `.xlsx` i `.pdf` format.

**Autentifikacija i RBAC** — JWT autentifikacija sa tri uloge:
- `EMPLOYEE` — pregled i logovanje
- `MANAGER` — dodavanje i uređivanje
- `OWNER` — puni pristup uključujući brisanje i administraciju

**Dinamičke kategorije** — Tipovi komponenti nisu hardkodirani; administratori mogu dodavati nove kategorije (npr. "VR slušalice", "Sim Racing volani").

---

## Baza podataka

PostgreSQL šema sa **8 tabela**, **1 view-om** i **1 triggerom**:

- `app_user`, `workstation`, `component`, `component_category`, `component_status`, `workstation_status`, `maintenance_type`, `maintenance_log`
- View `view_maintenance_status` — computed status za svaki par (radna stanica × tip održavanja)
- Trigger `trg_restrict_component_delete` — sprječava brisanje komponente dok je instalirana

---

## Sigurnost

- Lozinke se čuvaju BCrypt hashom
- JWT token sadrži email i ulogu korisnika
- `@PreAuthorize` anotacije na servisnom sloju za fine-grained kontrolu pristupa
- Svi osjetljivi parametri (DB credentials, JWT secret) se učitavaju iz environment varijabli

---

## Greške i validacija

- RFC 7807 `ProblemDetail` format za sve API greške
- Validacija na nivou DTO-a (`@NotBlank`, `@Email`, `@NotNull`, `@Size`)
- `GlobalExceptionHandler` mapira sve exception-e u konzistentne HTTP odgovore
- Korisniku nikad nije izložen stack trace ni SQL poruka
