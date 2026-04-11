# boat-app
A small but complete fullstack web application allowing authenticated users to manage a fleet of boats.

---

## Backend — `boat-app-backend`

### Tech stack

| Layer | Technology                                                         |
|-------|--------------------------------------------------------------------|
| Language | Java 25                                                            |
| Framework | Spring Boot 3.4.x                                                  |
| Persistence | Spring Data JPA + Hibernate + H2 (dev)                             |
| Mapping | MapStruct 1.6.3                                                    |
| Boilerplate | Lombok                                                             |
| Security | Spring Security (permit-all, extensible)                           |
| API docs | SpringDoc OpenAPI / Swagger UI — multi-version via `GroupedOpenApi` |
| Observability | Spring Boot Actuator                                               |
| Build | Maven 3.9+                                                         |

### Quick start (dev profile)

```bash
cd boat-app-backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The app starts on **http://localhost:8080** with an H2 in-memory database.

| URL | Description |
|-----|-------------|
| `GET /api/v1/boats/hello` | Smoke-test endpoint — returns 200 |
| `GET /actuator/health` | Health check (full details in dev) |
| `GET /swagger-ui.html` | Interactive Swagger UI (version dropdown top-right) |
| `GET /swagger-ui.html?urls.primaryName=v1` | Swagger UI pre-selected on v1 |
| `GET /v3/api-docs/v1` | Raw OpenAPI JSON spec for v1 |
| `GET /v3/api-docs.yaml` | Full OpenAPI YAML spec |
| `GET /h2-console` | H2 browser console (dev only) |

### Build

```bash
# Compile + run tests
mvn clean verify

# Package without tests (CI fast path)
mvn clean package -DskipTests
```

MapStruct generates `BoatMapperImpl` in `target/generated-sources/annotations/` during the `compile` phase.

### Profile strategy

| Profile | Database | DDL | SQL logging | Health details |
|---------|----------|-----|-------------|----------------|
| `dev` (default) | H2 in-memory | `create-drop` | `true` | `always` |
| `prod` | Configured via env vars | `validate` | `false` | `never` |

All profile names are centralised in `AppProfile` enum — no raw strings scattered in the code.

Switch profile at runtime:
```bash
# As JVM arg
java -jar boat-app-backend.jar --spring.profiles.active=prod

# Via environment variable (Docker / Kubernetes friendly)
SPRING_PROFILES_ACTIVE=prod java -jar boat-app-backend.jar
```

> **Cloud readiness** — all sensitive production values (datasource URL, credentials, etc.)
> must be injected via environment variables. No secrets are hardcoded.

### Adding a new API version

1. Create `BoatControllerV2` mapped to `/api/v2/boats`.
2. Uncomment (or add) the `v2Group()` bean in `OpenApiConfig`.
3. Swagger UI will show a `v1 / v2` dropdown automatically.
4. Annotate v1 endpoints with `@Deprecated` and add a sunset note in the v1 customizer.
5. Remove the v1 group bean once all clients have migrated.

### Package structure

```
com.boatapp.backend
├── BoatAppApplication.java      # Entry point — @SpringBootApplication + @EnableJpaAuditing
│
├── entity/
│   ├── Auditable.java           # @MappedSuperclass — createdAt + updatedAt inherited by all entities
│   └── Boat.java                # JPA entity — extends Auditable
│
├── dto/
│   └── BoatRecord.java          # Immutable Java record (id, name, description, createdAt, updatedAt)
│
├── mapper/
│   └── BoatMapper.java          # MapStruct interface → generates BoatMapperImpl
│
├── repository/
│   └── BoatRepository.java      # Spring Data JPA repository (CRUD + pagination)
│
├── service/
│   ├── IBoatService.java        # Interface (contract) — controllers depend on this
│   └── BoatServiceImpl.java     # Default implementation — throws 404 ResponseStatusException
│
├── controller/
│   ├── BoatControllerV1.java        # @RestController /api/v1/boats
│   └── GlobalExceptionHandler.java  # @RestControllerAdvice — uniform JSON error envelope
│
├── security/
│   └── SecurityConfig.java      # Permit-all, CSRF disabled, stateless session, H2 frames
│
└── config/
    ├── AppProfile.java          # Enum of all Spring profiles — single source of truth
    └── OpenApiConfig.java       # GroupedOpenApi per version + global OpenAPI metadata
```
