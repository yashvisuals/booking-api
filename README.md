# booking-api

An appointment booking API built with Spring Boot. Providers define weekly
availability; customers book time slots. Times are timezone-aware and the
database guarantees a slot can never be double-booked.

**Live demo:** https://booking-web-nine-vert.vercel.app
&nbsp;·&nbsp; **Live API:** https://booking-api-5teu.onrender.com/api/health
&nbsp;·&nbsp; **Frontend:** [booking-web](https://github.com/yashvisuals/booking-web)
_(free tier — the first request may take ~30s while the API wakes up)_

## Stack

- Java 21 + Spring Boot 4
- Spring Web (REST), Spring Security + JWT
- Spring Data JPA / Hibernate
- H2 (local) — swappable for MySQL
- JUnit 5 tests

## Notable bits

- **Timezone handling** — availability is defined in the provider's zone, stored
  and served as UTC instants; the client renders local time.
- **Slot generation** — bookable slots are computed from availability rules for a
  given date, with already-booked slots removed.
- **Conflict detection** — a unique constraint on `(provider, start_time)` means
  two concurrent bookings for the same slot can never both succeed. There's a
  test that fires two bookings in parallel and asserts exactly one wins.

## Running it

Needs Java 21+. No database install — uses an H2 file under `data/`.

```bash
./mvnw spring-boot:run      # Windows: .\mvnw.cmd spring-boot:run
```

App runs on http://localhost:8123. H2 console at `/h2-console`
(JDBC URL `jdbc:h2:file:./data/booking`, user `sa`, no password).

## API

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/register` | – | create account (PROVIDER or CUSTOMER), returns JWT |
| POST | `/auth/login` | – | returns JWT |
| POST | `/availability` | PROVIDER | add a weekly availability rule |
| GET | `/availability` | PROVIDER | list own rules |
| DELETE | `/availability/{id}` | PROVIDER | delete a rule |
| GET | `/providers/{id}/slots?date=YYYY-MM-DD` | any | free slots for that date |
| POST | `/bookings` | any | book a slot `{ providerId, start }` |
| GET | `/bookings/me` | any | your bookings |
| POST | `/bookings/{id}/cancel` | any | cancel a booking |

Authenticated calls need an `Authorization: Bearer <token>` header.

## Tests

```bash
./mvnw test
```
