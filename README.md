# boat-app

A fullstack web application for managing a fleet of boats.

---

## 🚀 Quick Start

```bash
docker-compose up --build
```

That's it. The startup order (`auth-mock → postgres → backend → frontend`) is handled automatically via healthchecks.

---

## 🌐 URLs

| Service | URL | Description |
|---------|-----|-------------|
| **App** | http://localhost:4200 | Angular frontend |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Interactive API docs |
| **Keycloak** | http://localhost:9000 | Auth admin console |

---

## 🔐 Test Credentials

### App login (http://localhost:4200)

| Role | Username | Password | Permissions |
|------|----------|----------|-------------|
| **User** | `user` | `user123` | Read boats |
| **Admin** | `admin` | `admin123` | Read, create, update, delete boats |

### Keycloak admin console (http://localhost:9000)

| Username | Password |
|----------|----------|
| `admin` | `admin` |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | Angular 20 — Standalone, Zoneless, Signals, Angular Material 3, Tailwind CSS |
| Backend | Java 25 · Spring Boot 3 · Spring Security (JWT/PKCE) · Spring Data JPA |
| Database | PostgreSQL 16 |
| Auth | Keycloak 24 — OAuth2 Authorization Code + PKCE |
| API Docs | SpringDoc OpenAPI / Swagger UI |
| Build | Maven 3.9 · Angular CLI 20 · Docker Compose |

---

## 📋 Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose)

No local Java, Node.js, or database installation required — everything runs in containers.

---

## 🏗️ Architecture Overview

### Backend (`boat-app-backend`)

Clean layered architecture: `Controller → Service → Repository`

```
BoatControllerV1  →  IBoatService / BoatServiceImpl  →  BoatRepository (JPA)
       ↓                        ↓
GlobalExceptionHandler      BoatMapper (MapStruct)  →  BoatRecord (DTO / Java record)
```

- Entities never exposed directly — always mapped to an immutable `BoatRecord` via MapStruct
- `Auditable` superclass provides `createdAt` (UTC `Instant`) inherited by all entities
- `Permission` enum is the single source of truth for role→method→endpoint rules

### Frontend (`boat-app-frontend`)

Feature-based structure, fully standalone (no NgModules):

```
app/
├── core/          # HTTP services, error interceptor
├── features/
│   └── boats/     # List, card, detail dialog, form dialog (create/edit)
├── shared/        # Reusable components & models
└── i18n/          # All UI labels centralised in en-EN.ts
```

- **Zoneless** — signals replace `NgZone` and RxJS-based state management
- All API calls proxied through nginx (`/api/` → `backend:8080`) — zero CORS issues
- `sessionStorage` for token storage (deliberate demo choice — HttpOnly Cookie is the production recommendation)

---

## 🔑 Security — Permission Matrix

Roles are extracted from the `realm_access.roles` claim in the Keycloak JWT.
The `Permission` enum is the single source of truth — `SecurityConfig` reads from it directly.

| Permission | Role | Method | Endpoint |
|------------|------|--------|----------|
| `BOATS_READ` | `ROLE_USER` | GET | `/api/v1/boats/**` |
| `BOATS_CREATE` | `ROLE_ADMIN` | POST | `/api/v1/boats` |
| `BOATS_UPDATE` | `ROLE_ADMIN` | PUT | `/api/v1/boats/**` |
| `BOATS_DELETE` | `ROLE_ADMIN` | DELETE | `/api/v1/boats/**` |

Always public: `GET /actuator/health`, `GET /actuator/info`  
Dev only: `GET /swagger-ui/**`, `GET /v3/api-docs/**`

---

## ⚙️ Spring Profiles

| Profile | Activated by | DB DDL | SQL logs | Swagger | Health details |
|---------|-------------|--------|----------|---------|----------------|
| `dev` | default / Docker Compose | `create-drop` + `data.sql` (10 seed boats) | enabled | ✅ public | `always` |
| `prod` | `SPRING_PROFILES_ACTIVE=prod` | `validate` | disabled | ❌ hidden | `never` |

- **Dev** credentials use `${ENV_VAR:default}` — safe fallbacks for local use
- **Prod** credentials use `${ENV_VAR}` — no defaults, no hardcoding
- Profile names are centralised in `AppProfile` enum — no raw strings in code

---

## 🤖 AI Usage

This project was built using a structured AI-assisted workflow.

| File / Folder | Description |
|---------------|-------------|
| [`AI_USAGE.md`](./AI_USAGE.md) | Full methodology, example prompt, and what was deliberately NOT delegated to AI |
| [`.github/agents/`](./.github/agents/) | Custom `@fullstack` agent definition |
| [`.github/skills/`](./.github/skills/) | Skills: `task-driven-development`, `java-best-practices`, `java-testing`, `angular-best-practices`, `angular-component-creator` |

---

## 🧹 Stop & Clean Up

```bash
# Stop containers (keep data)
docker-compose down

# Stop containers and wipe the database volume
docker-compose down -v
```
