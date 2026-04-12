---
name: task-driven-development
description: Apply before ANY development task — backend or frontend.
  Clarifies requirements, defines acceptance criteria, implements with
  tests, verifies before finishing. Use when starting any feature,
  endpoint, component, fix or refactoring.
  Triggers on create, add, implement, build, fix, refactor, feature.
allowed-tools: ["read", "edit", "search/codebase", "terminal"]
---

# Task-Driven Development

Apply this skill at the START of every task — before writing any code.

## Step 1 — Read before writing
Understand what already exists before touching anything.

Backend: read the classes in the affected package, existing tests,
the entity and DTO if data is involved, SecurityConfig if a new
endpoint is added.
Frontend: read the existing components in the feature folder,
src/assets/i18n/en-EN.json, existing services if HTTP is needed,
app.routes.ts if a new page is added.

## Step 2 — Ask if ambiguous
If business rules, edge cases, security or UI behavior are unclear,
stop and ask before writing any code. Group questions clearly:

"Before I start, I need to clarify:
1. [business rule or edge case]
2. [security or behavior question]
3. [technical design choices]
   Waiting for answers before proceeding."

Common things to ask:
- Backend: deletion rules, edge cases on null/empty, auth requirements,
  response shape, error behavior (404 vs empty list), type of the data, tech design choices
- Frontend: form reset after submit, error display style (inline vs toast),
  redirect after save, empty state content
- Full-stack: request/response shape, expected HTTP codes,
  optimistic update vs wait for API

Skip this step only if the task is completely unambiguous.

## Step 3 — Acceptance criteria

### Scenario A — AC provided in the task
Acknowledge them and proceed immediately:
"Acceptance criteria found in task:
AC1: ...
AC2: ...
Proceeding to implementation."

### Scenario B — No AC in the task
Derive them yourself, state them, wait for confirmation:
"No AC provided. I will implement and verify:
AC1: ...
AC2: ...
Does this match your expectations?"

For full-stack tasks, label by layer:
AC1 (backend):  POST /api/v1/boats returns 201 with BoatRecord
AC2 (backend):  POST /api/v1/boats returns 400 when name is null
AC3 (frontend): form submits and new boat appears in the list
AC4 (frontend): validation error shown when name is empty

## Step 4 — Write tests 
For each AC: write failing test (red) → implement (green) → refactor.

Backend test mapping:
Controller behavior  → @WebMvcTest + MockMvc
Service logic        → unit test + Mockito
Repository queries   → @DataJpaTest
Full flow            → @SpringBootTest + TestContainers

Frontend test mapping:
Component rendering  → Angular TestBed + ComponentFixture
User interactions    → triggerEventHandler or userEvent
HTTP calls           → HttpClientTestingModule

Full-stack: all backend ACs must be green before starting frontend.

follow the skill /java-testing for all backend test conventions (AAA, naming).

## Step 5 — Implement production code
Backend:  follow /java-best-practices — layered architecture,
constructor injection, no null returns, Javadoc.
Frontend: follow /angular-best-practices and use
/angular-component-creator for any new component.

One concern at a time — never mix backend and frontend in one step.

## Step 6 — Verify and report all ACs
Run the tests and report explicitly for every AC:

Backend:  mvn test -pl boat-app-backend -Dtest=[TestClass]
Frontend: ng test --include=[spec-file] --watch=false

"Acceptance criteria verification:
✅ AC1 (backend):  createBoat_should_return201_when_inputIsValid — PASSED
✅ AC2 (backend):  createBoat_should_return400_when_nameIsNull — PASSED
✅ AC3 (frontend): form submits and updates list — PASSED
✅ AC4 (frontend): shows error on empty name — PASSED
All ACs passed. Task complete."

If any AC fails — fix and re-run. Never declare done with a failing test.

## Step 7 — Self-review before finishing

Backend:
- [ ] All ACs have a passing test
- [ ] No business logic in controllers
- [ ] No entity exposed directly — always DTO/Record via MapStruct
- [ ] No hardcoded values
- [ ] New exceptions handled in GlobalExceptionHandler
- [ ] New endpoints covered in SecurityConfig
- [ ] Javadoc on all new public service methods
- [ ] @Slf4j added on new service — debug/warn/error at the right levels

Frontend:
- [ ] All ACs have a passing test
- [ ] No hardcoded text — all strings in en-EN.json
- [ ] Mobile-first layout verified at 320px and 768px
- [ ] No any, no ! in TypeScript
- [ ] New page has lazy route in app.routes.ts
- [ ] Reusable components placed in shared/components/