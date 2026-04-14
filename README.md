# boat-app
A fullstack web application for managing a fleet of boats.

---

## Architecture

```
docker-compose up --build
       │
       ├─ auth-mock  (port 9000) — Keycloak 24 (OAuth2 Authorization Code + PKCE)
       ├─ postgres   (port 5432) — PostgreSQL 16
       ├─ backend    (port 8080) — Spring Boot 3 REST API
       └─ frontend   (port 4200) — Angular 20 served by nginx
```

Startup order enforced by `depends_on` + healthchecks:
`auth-mock → postgres → backend → frontend`

---

## Backend — `boat-app-backend`

### Tech stack

| Layer | Technology |
|-------|------------|
| Language | Java 25 |
| Framework | Spring Boot 3.4.x |
| Persistence | Spring Data JPA + Hibernate + PostgreSQL |
| Mapping | MapStruct 1.6.3 |
| Boilerplate | Lombok |
| Security | Spring Security — JWT resource server (Keycloak) + role-based access via `Permission` enum |
| API docs | SpringDoc OpenAPI / Swagger UI — multi-version via `GroupedOpenApi` |
| Observability | Spring Boot Actuator |
| Build | Maven 3.9+ |

### Quick start (dev profile)

**Prerequisites:** a PostgreSQL instance running on `localhost:5432` with database `boatdb`, user/password `boat`.
The easiest way is to start only postgres via Docker:

```bash
docker-compose up postgres
```

Then start the backend:

```bash
cd boat-app-backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The app starts on **http://localhost:8080** and seeds 10 boats via `data.sql` on every startup (dev only).

| URL | Description |
|-----|-------------|
| `GET /api/v1/boats` | Paginated list of boats (`?page=0&size=5`) |
| `GET /api/v1/boats/hello` | Smoke-test endpoint — returns 200 |
| `GET /actuator/health` | Health check (full details in dev) |
| `GET /swagger-ui.html` | Interactive Swagger UI (version dropdown top-right) |
| `GET /v3/api-docs/v1` | Raw OpenAPI JSON spec for v1 |

### Build & tests

```bash
# Unit tests only (BoatControllerTest + BoatServiceTest) — no DB needed, safe in Docker
mvn clean package

# Full suite: unit tests + BoatRepositoryIT (Testcontainers — requires Docker)
mvn clean verify
```

MapStruct generates `BoatMapperImpl` in `target/generated-sources/annotations/` during `compile`.

> **Test split:** unit tests run via `maven-surefire-plugin`; integration tests (`*IT.java`)
> run via `maven-failsafe-plugin` only during the `verify` phase. The Dockerfile uses
> `mvn clean package` so the Docker build never needs Docker-in-Docker.

### Profile strategy

| Profile | Database | DDL | SQL logging | Health details |
|---------|----------|-----|-------------|----------------|
| `dev` (default) | PostgreSQL (localhost / Docker) | `create-drop` + `data.sql` | `true` | `always` |
| `prod` | Configured via env vars | `validate` | `false` | `never` |

All profile names are centralised in `AppProfile` enum — no raw strings scattered in the code.

**Dev credentials** use `${ENV_VAR:default}` — override with environment variables.
**Prod credentials** use `${ENV_VAR}` — no defaults, no hardcoding.

Switch profile at runtime:

```bash
# JVM arg
java -jar boat-app-backend.jar --spring.profiles.active=prod

# Environment variable (Docker / Kubernetes friendly)
SPRING_PROFILES_ACTIVE=prod java -jar boat-app-backend.jar
```

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
│   ├── Auditable.java           # @MappedSuperclass — createdAt (Instant, UTC) for all entities
│   └── Boat.java                # JPA entity — extends Auditable, index on created_at DESC
│
├── dto/
│   └── BoatRecord.java          # Immutable Java record (id, name, description, createdAt)
│
├── mapper/
│   └── BoatMapper.java          # MapStruct interface → generates BoatMapperImpl
│
├── repository/
│   └── BoatRepository.java      # Spring Data JPA (JpaRepository — pagination built-in)
│
├── service/
│   ├── IBoatService.java        # Interface — controllers depend on this, not the impl
│   └── BoatServiceImpl.java     # findAll(page, size) — sorted by createdAt DESC
│
├── controller/
│   ├── BoatControllerV1.java        # @RestController /api/v1/boats — GET list + GET hello
│   └── GlobalExceptionHandler.java  # @RestControllerAdvice — uniform JSON error envelope
│
├── security/
│   └── SecurityConfig.java      # Permit-all, CSRF disabled, stateless
│
└── config/
    ├── AppProfile.java          # Enum of all Spring profiles — single source of truth
    └── OpenApiConfig.java       # GroupedOpenApi per version + global OpenAPI metadata
```

---

## Frontend — `boat-app-frontend`

### Tech stack

| Layer | Technology |
|-------|------------|
| Language | TypeScript (strict mode) |
| Framework | Angular 20 — standalone components, no NgModules |
| Change detection | **Zoneless** (`provideZonelessChangeDetection`) — no zone.js |
| State management | Angular **Signals** + `computed()` — no NgRx |
| UI components | Angular Material 20 — **Material Design 3 (M3)** |
| Layout | **Tailwind CSS v3** — mobile-first, custom breakpoints aligned with Material |
| Styles | SCSS throughout |
| HTTP | `HttpClient` + functional interceptor |
| Build | Angular CLI 20 / `@angular/build` (esbuild) |
| Container | Multi-stage Docker build → nginx:alpine |

### Angular 20 highlights

- **Signals everywhere** — `signal()`, `computed()`, `input()`, `output()` (no `@Input()`/`@Output()`)
- **Zoneless** — faster, simpler, no `NgZone` hacks; all change detection is explicit via signals
- **Standalone components** — no `NgModule`; imports declared per component
- **`inject()`** — function-based DI, no constructor injection needed
- **New file naming** — Angular CLI 20 generates `feature.ts / feature.html / feature.scss`

### Quick start (dev)

```bash
cd boat-app-frontend
ng serve
```

Opens on **http://localhost:4200** with live reload.

| URL | Description |
|-----|-------------|
| `/` | Redirects to `/home` |
| `/home` | Paginated grid of boat cards loaded from the API |

### Build

```bash
npm run build
# equivalent to: ng build --configuration=production
```

### Run with Docker (standalone)

```bash
cd boat-app-frontend
docker build -t boat-app-frontend .
docker run -p 80:80 boat-app-frontend
# open http://localhost
```

### Mobile-first conventions

Every component follows **mobile-first** Tailwind:

```html
<!-- ✅ correct: base = mobile, then override at sm/md/lg -->
<div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
```

Minimum interactive tap target: **48 × 48 px** (`mat-icon-button` enforces this by default).

### Tailwind breakpoints

Aligned with Angular Material in `tailwind.config.js`:

| Tailwind prefix | px | Angular Material equivalent |
|-----------------|-----|------------------------------|
| _(base)_ | 0–599 | Handset / XSmall |
| `sm:` | 600px | Small |
| `md:` | 960px | Medium |
| `lg:` | 1280px | Large |
| `xl:` | 1920px | XLarge |

### Project structure

```
src/
├── app/
│   ├── core/
│   │   ├── interceptors/
│   │   │   └── http-error.interceptor.ts  # Functional HTTP error interceptor
│   │   └── services/
│   │       ├── api.service.ts             # Base HTTP service (generic get<T>)
│   │       ├── boat.service.ts            # getBoats(page, size) → Observable<Page<Boat>>
│   │       └── translation.service.ts    # Signal-based i18n — setLocale() for runtime switch
│   │
│   ├── features/
│   │   └── boats/
│   │       ├── boats.component.ts         # Main list — signals, pagination, dialog opener
│   │       ├── boats.component.html       # Grid + spinner + paginator + page-size selector
│   │       └── components/
│   │           ├── boat-card/             # 🚢 emoji + name + eye/edit/delete icons
│   │           └── boat-detail-dialog/    # MatDialog — name, description, createdAt (UTC)
│   │
│   ├── shared/
│   │   ├── components/
│   │   │   └── page-header/              # Reusable header — input.required<string>()
│   │   └── models/
│   │       ├── api-error.model.ts        # ApiError — mirrors backend JSON error envelope
│   │       ├── boat.model.ts             # Boat { id, name, description, createdAt }
│   │       └── page.model.ts             # Page<T> — Spring Data Page response shape
│   │
│   ├── app.ts                            # App shell — toolbar + responsive M3 sidenav
│   ├── app.html
│   ├── app.config.ts                     # provideZonelessChangeDetection + routes + http
│   └── app.routes.ts                     # '' → /home, /home → BoatsComponent (lazy)
│
├── environments/
│   ├── environment.ts                    # dev:  apiUrl = http://localhost:8080/api/v1
│   └── environment.prod.ts              # prod: apiUrl = ${API_URL}
│
├── i18n/
│   └── en-EN.ts                          # All UI labels — single source of truth
│
├── styles/
│   ├── _material-theme.scss             # M3 theme — indigo primary, teal secondary
│   └── _variables.scss                  # SCSS design tokens
│
└── styles.scss                           # Entry: material-theme → @tailwind base/components/utilities
```

### Adding a new feature

1. Create `src/app/features/my-feature/my-feature.component.ts` (standalone)
2. Add a lazy route in `app.routes.ts`:
   ```ts
   { path: 'my-feature', loadComponent: () => import('./features/my-feature/my-feature.component').then(m => m.MyFeatureComponent) }
   ```
3. Add a nav link in `app.html` inside `<mat-nav-list>`
4. Add i18n keys in `src/i18n/en-EN.ts`
5. (Optional) Add a feature service in `core/services/my-feature.service.ts`

---

## Full stack — Docker Compose

```bash
# Start everything (auth-mock → postgres → backend → frontend)
docker-compose up --build

# Stop and keep the postgres volume
docker-compose down

# Also wipe the database volume
docker-compose down -v
```

> ⚠️ **Windows + Git Bash / MinGW64:** the long-form volume syntax (`type: volume / target:`)
> is already used in `docker-compose.yml` to prevent Git Bash from translating the PostgreSQL
> container path into a Windows host path (which causes a `pg_control` panic).
> If you still encounter issues, use **PowerShell** instead of Git Bash, or prefix commands
> with `MSYS_NO_PATHCONV=1`. If the volume is already corrupted: `docker-compose down -v`.

| Service | Port | Notes |
|---------|------|-------|
| frontend | http://localhost:4200 | nginx — proxies `/api/` → backend:8080 |
| backend | http://localhost:8080 | Spring Boot dev profile |
| postgres | localhost:5432 | Persistent volume `postgres_data` |
| auth-mock | http://localhost:9000 | Keycloak 24 — realm `boat-app` |

> **nginx proxy:** all API calls from the browser route through nginx (`/api/` → `backend:8080`)
> so there are zero CORS issues regardless of environment.

---

## Keycloak — `boat-app-auth-mock`

Keycloak 24 is configured automatically via a realm import on startup.
**No manual steps in the admin console are required.**

### Access

| URL | Description |
|-----|-------------|
| `http://localhost:9000` | Keycloak admin console |
| `http://localhost:9000/realms/boat-app/.well-known/openid-configuration` | OIDC discovery endpoint |
| `http://localhost:9000/realms/boat-app/protocol/openid-connect/certs` | JWKS — RSA public keys |

**Admin console credentials:** `admin` / `admin`

### Realm & Client

| Setting | Value |
|---------|-------|
| Realm | `boat-app` |
| Client ID | `boat-frontend` |
| Allowed flow | Authorization Code + **PKCE (S256)** only |
| Password grant | ❌ disabled (`directAccessGrantsEnabled: false`) |
| Implicit flow | ❌ disabled |
| Access token TTL | 300 s (5 min) |
| SSO session max | 1800 s (30 min) |

### Test users

| Username | Password | Roles |
|----------|----------|-------|
| `user` | `user123` | `ROLE_USER` |
| `admin` | `admin123` | `ROLE_USER`, `ROLE_ADMIN` |

### Why PKCE is enforced

`pkce.code.challenge.method: S256` is set inside the `attributes` block of the
`boat-frontend` client. This forces Keycloak to reject any authorisation request
that does not include a valid `code_challenge`. The Angular app must always use
the **Authorization Code + PKCE** flow — the Password grant is intentionally
disabled to follow OAuth2 best practices for SPAs (RFC 9700 / BCP 212).

### Quick verification

```bash
# Discovery endpoint — must contain "code_challenge_methods_supported": ["S256"]
curl http://localhost:9000/realms/boat-app/.well-known/openid-configuration | jq .

# JWKS — must return at least one RSA key
curl http://localhost:9000/realms/boat-app/protocol/openid-connect/certs | jq .

# Password grant — must return 401/400 (intentionally disabled)
curl -X POST http://localhost:9000/realms/boat-app/protocol/openid-connect/token \
  -d "grant_type=password&client_id=boat-frontend&username=user&password=user123"
```

---

## Security — Permission Matrix

All endpoints are protected by JWT-based role access control.
Roles are extracted from the `realm_access.roles` claim in the Keycloak JWT.
Permissions are defined centrally in `Permission.java` — adding a new permission
requires only adding a value to that enum, no changes to SecurityConfig.

| Permission   | Role       | Method | Endpoint         |
|--------------|------------|--------|------------------|
| BOATS_READ   | ROLE_USER  | GET    | /api/v1/boats/** |
| BOATS_CREATE | ROLE_ADMIN | POST   | /api/v1/boats    |
| BOATS_UPDATE | ROLE_ADMIN | PUT    | /api/v1/boats/** |
| BOATS_DELETE | ROLE_ADMIN | DELETE | /api/v1/boats/** |

### Always public (all profiles)
- `GET /actuator/health`
- `GET /actuator/info`

### Public in dev/staging only
- `GET /v3/api-docs/**`
- `GET /swagger-ui/**`
- `GET /swagger-ui.html`

### Test users (demo)
| Username | Password | Roles                  |
|----------|----------|------------------------|
| user     | user123  | ROLE_USER              |
| admin    | admin123 | ROLE_USER, ROLE_ADMIN  |

---

## Timestamps

All timestamps use **`Instant` (UTC)**. Three layers enforce UTC end-to-end:

| Layer | Config | Effect |
|-------|--------|--------|
| JVM | `-Duser.timezone=UTC` in Docker `ENTRYPOINT` | Java system timezone = UTC |
| Hibernate → JDBC | `hibernate.jdbc.time_zone: UTC` in `application.yml` | Timestamps sent and read as UTC on the JDBC wire |
| PostgreSQL | `TIMESTAMP WITH TIME ZONE` column type | Storage is timezone-aware |

The Angular frontend formats timestamps with `DatePipe` using the `'UTC'` timezone.
