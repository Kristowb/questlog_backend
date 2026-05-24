# QuestLog Backend

Backend REST API untuk **QuestLog: Fitness & Feast** — Spring Boot 3, Java 21, PostgreSQL.

---

## Arsitektur

```
Controller → Service (interface) → ServiceImpl → Repository → PostgreSQL
     ↓              ↓
  @Valid DTO    Business Logic
```

### Struktur Paket

| Paket | Tanggung Jawab |
|-------|---------------|
| `config/` | Spring Beans — CORS global (`WebConfig`), Google OAuth (`GoogleAuthConfig`) |
| `controller/` | REST endpoint `/api/v1/**`, menerima request, delegasi ke service |
| `dto/` | Java Record — request (+ Jakarta Validation) & response (+ `fromEntity`) |
| `exception/` | `AbstractThrowableProblem` (Zalando) + `GlobalExceptionHandler implements ProblemHandling` |
| `model/` | JPA Entity — Lombok (`@Getter @Setter @Builder`), tanpa `@Data` |
| `repository/` | Spring Data JPA interface |
| `service/` | Interface + `impl/` — business logic, injeksi via konstruktor |

---

## Konvensi Kode

**Controller** — 2 baris per method: panggil service, lalu return response.
```java
@PostMapping
public ResponseEntity<DietLogResponse> add(@Valid @RequestBody DietLogRequest request) {
    DietLogResponse response = dietService.addDietLog(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

**DTO** — Java Record + Jakarta Validation.
```java
public record DietLogRequest(
    @NotNull Long userId,
    @NotBlank String foodName,
    @PositiveOrZero double calories
) {}
```

**Exception** — Zalando `AbstractThrowableProblem`, self-describing RFC 7807.
```java
public class ResourceNotFoundException extends AbstractThrowableProblem {
    public ResourceNotFoundException(String msg) {
        super(TYPE, "Resource Not Found", Status.NOT_FOUND, msg);
    }
}
```

**GlobalExceptionHandler** — Kosong. `ProblemHandling` menangani semua exception otomatis.

---

## Stack
    
| Layer | Teknologi |
|-------|-----------|
| Runtime | Java 21 LTS (Corretto/Temurin) |
| Framework | Spring Boot 3.4.3 |
| Validasi | `spring-boot-starter-validation` (Jakarta) |
| Error Handling | `problem-spring-web-starter` 0.29.1 (Zalando RFC 7807) |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL (Supabase) |
| Auth | Google OAuth2 ID Token |
| Payment | Stripe (mock) |
| Build | Lombok 1.18.46, Maven |

## Deployment

- **Host**: Railway — baca `PORT` env otomatis
- **DB**: Supabase PostgreSQL — credential via env variable (`SUPABASE_DB_PASSWORD`)
- **DDL**: `spring.jpa.hibernate.ddl-auto=update`
