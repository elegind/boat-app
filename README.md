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
│   ├── Auditable.java           # @MappedSuperclass — createdAt inherited by all entities
│   └── Boat.java                # JPA entity — extends Auditable
│
├── dto/
│   └── BoatRecord.java          # Immutable Java record (id, name, description, createdAt)
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

- **Signals everywhere** — `signal()`, `computed()`, `input()` (new input API, no `@Input()` decorator)
- **Zoneless** — faster, simpler, no `NgZone` hacks. All change detection is explicit via signals
- **Standalone components** — no `NgModule` at all; imports are declared per component
- **`inject()`** — function-based DI, no constructor injection needed in services
- **New file naming** — Angular CLI 20 generates `feature.ts / feature.html / feature.scss` (no `.component.` suffix)

### Quick start (dev)

```bash
cd boat-app-frontend
ng serve
```

Opens on **http://localhost:4200** with live reload.

| URL | Description |
|-----|-------------|
| `/home` | Home page with signal-based click counter |
| `/` | Redirects to `/home` |

### Build

```bash
# Dev build
ng build

# Production build
npm run build
# equivalent to: ng build --configuration=production
```

### Run with Docker

```bash
cd boat-app-frontend
docker build -t boat-app-frontend .
docker run -p 80:80 -e API_URL=http://my-api/api/v1 boat-app-frontend
```

The `API_URL` env var is injected into `environment.prod.ts` at runtime.

### Mobile-first conventions

Every component follows **mobile-first** Tailwind:

```html
<!-- ✅ correct: base styles for mobile, override at sm/md/lg -->
<div class="flex-col sm:flex-row p-4 md:p-6 w-full md:max-w-lg">
```

Minimum interactive tap target: **48 × 48 px** (`min-h-12` Tailwind class).

### Tailwind breakpoints

Custom breakpoints in `tailwind.config.js` **aligned with Angular Material**:

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
│   │       └── api.service.ts             # Base HTTP service (generic get/post/put/delete)
│   │
│   ├── features/
│   │   └── home/
│   │       ├── home.ts                    # Lazy-loaded home page with signal counter
│   │       ├── home.html
│   │       └── home.scss
│   │
│   ├── shared/
│   │   ├── components/
│   │   │   └── page-header/
│   │   │       ├── page-header.ts         # Reusable header — input.required<string>()
│   │   │       ├── page-header.html
│   │   │       └── page-header.scss
│   │   └── models/
│   │       └── api-error.model.ts         # ApiError interface — mirrors backend error envelope
│   │
│   ├── app.ts                             # App shell — toolbar + responsive sidenav
│   ├── app.html
│   ├── app.config.ts                      # provideZonelessChangeDetection + routes + http
│   └── app.routes.ts                      # Lazy-loaded routes
│
├── environments/
│   ├── environment.ts                     # dev: apiUrl = http://localhost:8080/api/v1
│   └── environment.prod.ts               # prod: apiUrl = ${API_URL} (injected at runtime)
│
├── styles/
│   ├── _material-theme.scss              # M3 theme — violet primary, cyan tertiary
│   └── _variables.scss                   # SCSS design tokens
│
└── styles.scss                            # Entry: @use material-theme → @tailwind → globals
```

### Adding a new feature

1. Create `src/app/features/my-feature/my-feature.ts` (standalone component)
2. Add a lazy route in `app.routes.ts`:
   ```ts
   { path: 'my-feature', loadComponent: () => import('./features/my-feature/my-feature').then(m => m.MyFeatureComponent) }
   ```
3. Add a nav link in `app.html` inside `<mat-nav-list>`
4. (Optional) Add a feature service in `src/app/features/my-feature/my-feature.service.ts`


