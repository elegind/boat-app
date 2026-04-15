# AI_USAGE.md

## Tools used

- **GitHub Copilot Chat (IntelliJ)** — code generation, testing, documentation,
  using the custom `@fullstack` agent. Model used: Claude Sonnet 4.6.
- **Claude (claude.ai)** — brainstorming, prompt design and review,
  technical advice.

---

## Methodology

AI was used in two roles: **advisor** to take decisions,
and **executor** for code generation.

### Custom agent and skills

With the help of AI, I created a custom `@fullstack` agent with a set of skills
that are invoked depending on the task. These files live in `.github/agents/`
and `.github/skills/` and follow the [Agent Skills specification](https://agentskills.io/specification):

- `task-driven-development` — asks clarifying questions if needed, derives
  acceptance criteria, step by step development (backend to frontend)
- `java-best-practices` — layered architecture, naming conventions, clean code
- `java-testing` — AAA pattern, test naming, unit and integration test rules
- `angular-best-practices` — signals, OnPush, i18n, mobile-first
- `angular-component-creator` — standalone component creation checklist

The `@fullstack` agent is used for every task and always starts by invoking
`task-driven-development` before any other skill.

### Task workflow

For every feature I followed this structured process:

1. **Draft the task manually** with a clear description covering: what needs to be built,
   which parts of the code are affected, mandatory tests, error handling,
   validation rules, technical choices, data formats, component reusability,
   and acceptance criteria.
2. **Ask AI to review the draft** — improve it, challenge it, ask
   clarifying questions if needed.
3. **Deeply review the final prompt** to make sure it contains only what I want 
   and discard hallucinations or unwanted changes before submitting it to Copilot.
4. **Submit to the `@fullstack` agent** which applies the task-driven-development
   skill first, then invokes the relevant skills for the task.
5. **Review the generated code carefully**, fix issues, iterate, run tests,
   then perform manual validation and manual fixes.

I applied this workflow to most coding tasks: backend bootstrap, frontend
bootstrap, Docker and docker-compose setup, Keycloak OAuth2 configuration,
all CRUD features, and test writing.

### Example task Prompt

Below is a representative example of a task prompt I gave to the custom agent.
It illustrates the level of detail and structure I used consistently.
Since you asked 3-5 prompts you can find some more prompts into .agents.
For this task, I asked the AI to create a reusable form dialog component for both
creation and edition of a boat:

---

```
Task: Create a boat

Description:
Use the @fullstack agent with task-driven-development, java-best-practices,
java-testing, angular-best-practices and angular-component-creator skills
to develop the boat creation feature.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
BACKEND
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Endpoint: POST /api/v1/boats
Request body: BoatRequest record
Response: 201 Created with BoatRecord body.

Create BoatRequest record in dto/:
  public record BoatRequest(
      @NotBlank
      @Size(max = 30)
      @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Name can only contain letters, numbers and hyphens")
      String name,

      @Size(max = 500)
      String description
  )

Service layer:
  createBoat(BoatRequest request): BoatRecord
  Map BoatRequest → Boat entity via MapStruct, save, return BoatRecord.
  createdAt and updatedAt are set automatically by @CreatedDate/@LastModifiedDate.
  No business exception needed for creation unless name uniqueness
  is required — ask before implementing a unique constraint.

Controller:
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BoatRecord create(@Valid @RequestBody BoatRequest request)

GlobalExceptionHandler already handles MethodArgumentNotValidException → 400
with field errors. No change needed.

Add toEntity() method to BoatMapper:
  Boat toEntity(BoatRequest request);

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FRONTEND
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Form approach — Reactive Forms:
Use ReactiveFormsModule with FormGroup and FormControl.
Reactive Forms are the correct choice here because:
- Validation logic stays in TypeScript, not in the template
- Easy to reset, patch and reuse for the update feature later
- Testable without DOM interaction
Template-driven forms would scatter validation across the template
and make reuse for update harder.

BoatService — add:
  createBoat(request: BoatRequest): Observable<Boat>
  POST environment.apiUrl + '/boats' with request body.

Create BoatRequest interface in shared/models/boat-request.model.ts:
  interface BoatRequest { name: string; description: string; }

BoatFormDialogComponent — features/boats/components/boat-form-dialog/
This dialog is designed to be reused for update — accept an optional
boat input for pre-filling in the future:
  boat = input<Boat | null>(null)   ← null = create mode, Boat = edit mode
  saved = output<Boat>()

Form definition:
  this.form = this.fb.group({
    name: ['', [
      Validators.required,
      Validators.maxLength(30),
      Validators.pattern(/^[a-zA-Z0-9- ]+$/)
    ]],
    description: ['', [
      Validators.maxLength(500)
    ]]
  });

On open in create mode: form is empty and reset.
On open in edit mode (future): patch form with boat values.
On confirm: emit saved output with the created/updated boat.
On cancel: close dialog and reset form.
The dialog itself does NOT call the service — the parent component
(BoatsComponent) handles the API call. The dialog only owns the form.

Display in the form:
- MatFormField + MatInput for name
- MatFormField + MatTextarea for description
- MatHint inside each field showing requirements:
    name:        "Letters, numbers and hyphens only. Max 30 characters."
    description: "Max 500 characters."
- MatError shown when field is invalid and touched:
    name required:    "Name is required."
    name maxlength:   "Name cannot exceed 30 characters."
    name pattern:     "Only letters, numbers and hyphens allowed."
    description max:  "Description cannot exceed 500 characters."
- Inputs highlighted in red automatically via Angular Material
  error state when invalid + touched.
- "Create" button disabled when form.invalid:
    [disabled]="form.invalid"

BoatsComponent — add create button and handle dialog:
  Place a MatButton with mat-raised-button at the top right of the
  boat grid:
    <button mat-raised-button color="primary">
      <mat-icon>add</mat-icon> Create boat
    </button>

  On click: open MatDialog with BoatFormDialogComponent (create mode).
  On saved output:
    1. Call boatService.createBoat(request)
    2. On success:
       - Show success snackbar
       - If current page is the last page and has room: add boat
         to boats signal locally.
       - Otherwise reload current page from API to reflect correct
         pagination state.
    3. On error: show error snackbar.

Page refresh after creation:
  Creation is more complex than deletion for pagination —
  the new boat may not belong on the current page depending on
  sort order (createdAt desc means it appears on page 0).
  Simplest correct approach: after creation, navigate to page 0
  and reload. This guarantees the new boat is visible immediately.

i18n — add to en-EN.json under "boats":
  "create.button": "Create boat",
  "create.dialog.title": "Create a new boat",
  "form.name.label": "Name",
  "form.name.hint": "Letters, numbers and hyphens only. Max 30 characters.",
  "form.name.error.required": "Name is required.",
  "form.name.error.maxlength": "Name cannot exceed 30 characters.",
  "form.name.error.pattern": "Only letters, numbers and hyphens allowed.",
  "form.description.label": "Description",
  "form.description.hint": "Max 500 characters.",
  "form.description.error.maxlength": "Description cannot exceed 500 characters.",
  "form.cancel": "Cancel",
  "form.create": "Create",
  "form.save": "Save",
  "create.success": "Boat successfully created",
  "create.error": "An error occurred while creating the boat"

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
TESTS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Backend:
  BoatControllerTest:
    should_return201_when_boatCreatedSuccessfully()
    should_return400_when_nameIsNull()
    should_return400_when_nameExceedsMaxLength()
    should_return400_when_nameContainsSpecialCharacters()
    should_return400_when_descriptionExceedsMaxLength()
  BoatServiceTest:
    should_returnBoatRecord_when_boatCreatedSuccessfully()

Frontend:
  BoatFormDialogComponent:
    should_disableCreateButton_when_formIsInvalid()
    should_showRequiredError_when_nameIsEmpty()
    should_showPatternError_when_nameContainsSpecialChars()
    should_showMaxLengthError_when_nameExceeds30Chars()
    should_showMaxLengthError_when_descriptionExceeds500Chars()
    should_emitSaved_when_formIsValidAndConfirmed()
    should_resetForm_when_cancelClicked()
  BoatsComponent:
    should_openFormDialog_when_createButtonClicked()
    should_showSuccessSnackbar_when_creationSucceeds()
    should_showErrorSnackbar_when_creationFails()
    should_navigateToFirstPage_when_boatCreatedSuccessfully()

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
ACCEPTANCE CRITERIA
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

AC1: Clicking "+ Create boat" opens BoatFormDialogComponent with empty fields.
AC2: Validation errors are displayed per field when touched and invalid.
     The Create button is disabled while form.invalid.
AC3: MatHint is visible inside each input describing the format
     requirements before the user types.
AC4: On successful creation, snackbar shows "Boat successfully created"
     and the list navigates to page 0 to show the new boat.
AC5: Name uses MatInput, description uses MatTextarea.
     Both respect max length and pattern constraints on backend
     (@Size, @Pattern, @Valid) and frontend (Validators).
```

---

After submitting this task to the agent, I carefully reviewed the generated
code, fixed issues, and iterated. For this specific task, I improved the
AI output by adding `maxlength` at the HTML input level so users physically
cannot type more than 30 characters — rather than just showing an error
after the fact.

---

## What I chose NOT to delegate to AI

### Stack and architecture decisions

- **SQL over NoSQL** — the schema is stable and predictable, always the same
  fields. MongoDB would have added complexity with no benefit here.
- **Long + SEQUENCE over UUID** — a database sequence with `allocationSize=50`
  means Hibernate fetches 50 IDs at once from the DB, resulting in far fewer
  roundtrips. It also produces human-readable IDs in URLs and works identically
  on some different SQL databases.
- **Java Date type: Instant over LocalDateTime** — guarantees UTC at every level and
  is robust across different server timezones and cloud regions. AI proposed LocalDateTime,
  I decided to change it.
- **Spring Boot 3.5 over 4** — Spring Boot 4 is too recent and third-party
  libraries may not be fully aligned yet. More comfortable developing on 3.x.
- **Java 25** — latest available version, acceptable for a demo project.
- **Bootstrap with H2 first, then PostgreSQL** — faster initial startup,
  then migrated to PostgreSQL once Docker was in place.
- **Layered architecture** — hexagonal or microservices would be overkill
  for this scope. A clean Controller → Service → Repository layering is
  the right fit.
- **Angular 20 with Signals and zoneless** — modern patterns preferred over
  older Zone.js and RxJS-based state management.
- **i18n from day one** — I decided to implement internationalisation and
  put all labels in `en-EN.json` from the start to make future language support straightforward 
  without refactoring templates.
- **DTO Record + MapStruct** — never expose JPA entities directly in API
  responses. I decided to use Records because it provides immutability 
  and MapStruct because it provides a clean separation between layers.
- **API versioning** — added `/v1` prefix from the start and used
  `GroupedOpenApi` to make adding future versions easy without restructuring.
- **Authorization Code + PKCE** — more secure than other flows for a Single page app.
  Keycloak was chosen for its official Docker image, start-dev mode,
  and realm import via JSON with no manual setup.
- **sessionStorage for token storage** — a deliberate and conscious choice
  for a demo project. I am aware that HttpOnly Cookie is the production
  recommendation as it protects against XSS. Implementing HttpOnly would
  require a BFF pattern which is out of scope here.
- **Index on `created_at DESC`** — boats are sorted by `created_at DESC`
  on every paginated query. I purposely created this index It will be useful when
  the table grows to thousands of rows.
- **Using TestContainers for Integration tests** 

### Design and security decisions

- **Permission matrix** — I designed the `Permission` enum and the role
  assignment (ROLE_USER reads, ROLE_ADMIN writes) myself. This is a business
  and security decision that requires domain understanding and cannot be
  delegated to AI.
- **Validation at service layer** — AI put validation only at the controller
  level. I added it at the service layer too, because if this service is
  called from a batch job or a Kafka consumer in the future, it needs to
  enforce constraints independently of API.
- **Field constraints** — the decisions on which fields are mandatory,
  their maximum lengths, and the allowed character patterns were made by me,
  not generated. Business logic/constraints should not be delegated to AI.
- **Unit and integration test coverage for critical paths** — I personally decided 
  which critical scenarios required dedicated tests, 
  especially around security: ensuring that
  protected endpoints return 401 when no token is provided, 403 when the
  role is insufficient, and that admin-only operations are correctly
  rejected for regular users. These security invariants must be verified
  by explicit tests.

### Methodology decisions

- **Full-feature tasks (frontend + backend together)** — vertical slices
  rather than horizontal layers. Each task delivers a complete, demonstrable
  feature.
- **Test naming convention** — `should_[expectedBehavior]_when_[condition]`
  enforced consistently across all tests.
- **AAA pattern** — every test structured with clearly labeled
  Arrange/Act/Assert sections.
- **Clean code** — small functions, single responsibility. This was a
  personal standard applied during review, not enforced by AI.
- **Direct commits to main** — deliberate choice for a solo demo project.
  In a real team, a branching strategy like Git Flow would be used.
- **Conventional commits** — all commit messages follow the convention
  for readability.
- **Token optimisation** — small changes and corrections were done manually
  or with a lighter model to avoid unnecessary token consumption on trivial
  changes.

### Manual validation

- Verified the `created_at` index was correctly created in PostgreSQL
  by connecting to the container and running:
  ```sql
  SELECT indexname, indexdef FROM pg_indexes WHERE tablename = 'boat';
  ```
- Validated the JWT token structure by decoding it on jwt.io — confirmed
  presence of `realm_access.roles`, `iat` and `exp` claims with correct values.
- Verified the `Authorization: Bearer` header was present on every API call
  using the browser DevTools Network tab.
- Manually tested Acceptance criteria defined on every tasks

---

## How I validated and corrected AI output

After each task I reviewed the output carefully, ran the tests, optimize or simplify the code (refactor)
and did manual checks before considering it done. Below are the most significant corrections and
improvements I made throughout the project.

**`Auditable` superclass:**
AI put `createdAt` and `updatedAt` directly on the `Boat` entity. I refactored
them into a shared `Auditable` superclass so future entities can inherit
audit fields without duplication.

**Swagger disabled in production:**
AI left Swagger accessible in all profiles. I explicitly requested it to be
disabled in production for security. No need to expose API documentation
publicly.

**UTC enforcement:**
AI proposed `LocalDateTime` for dates. I changed it to `Instant` and enforced UTC at
every level: JVM flag, Hibernate JDBC timezone, and JSON serialization.

**Validation at service layer:**
AI added validation only at the controller level via `@Valid`. I added Bean
Validation annotations at the service layer too, so constraints are enforced
regardless of how the service is invoked in the future (batchs etc..)

**API versioning:**
AI handled versioning incorrectly. I fixed it by adding `v1` to the controller
`RequestMapping` and using `GroupedOpenApi` to properly support multiple API
versions later.

**`"dev"` hardcoded in GlobalExceptionHandler:**
AI used a hardcoded `"dev"` string to detect the active Spring profile.
I replaced it with an `AppProfile` enum and a helper method to check active
profiles in a clean and reusable way.

**Hardcoded error messages in GlobalExceptionHandler:**
AI generated fixed, static error reason messages. I reviewed and corrected
them to be dynamic and consistent across all exception types.

**`requireHttps: false` in OAuth2 config:**
AI set `requireHttps` to `false` globally. I changed it to `remoteOnly`
so HTTPS is enforced in production environments but not blocked locally
during development.

**Boat components placed in `shared/`:**
AI placed `BoatCardComponent` and `BoatDetailDialogComponent` in
`shared/components/`. Since these components are specific to the boats domain
and not reused elsewhere, I moved them to `features/boats/components/`.

**Static labels in Angular bootstrap:**
The frontend bootstrap had hardcoded text directly in templates. I fixed
this by moving all labels to `en-EN.json` translation keys to prepare for i18n.

**`provideAnimations` deprecated:**
AI used `provideAnimations()` in the Angular app config. This is deprecated
since Angular 18 — the build system now includes animations automatically.
I removed it.

**Pagination — duplicate page selector:**
AI created a custom page size dropdown while `MatPaginator` already provides
one built in. I removed the duplicate to avoid confusion and redundant code.

**Paginator visible when list is empty:**
AI left the `MatPaginator` visible even when there were no results to display.
I fixed the condition so it is hidden when the list is empty.

**Delete — page not refreshing correctly:**
After deleting a boat, the signal update logic was not reloading the current
page properly, leaving stale data on screen. I fixed the update logic.

**Form disabled state color:**
Input fields were displaying an unexpected grey color when showing validation
errors. I fixed the Angular Material theme override for the error state.

**Docker — Spring profile injection:**
AI only set `SPRING_PROFILES_ACTIVE` in the docker-compose environment. I also
added it to the backend Dockerfile `ENTRYPOINT` so the backend can start
correctly on its own, independently of docker-compose.

**401 on all endpoints after securing them:**
After enabling Spring Security JWT validation, every request returned 401.
The issue was a Docker network split — the backend container could not reach
the Keycloak container to validate tokens. I fixed the internal Docker
network configuration.

**UserProfile model over-specified:**
AI included `email`, `given_name`, and `family_name` in the `UserProfile`
model. Since only `sub` and `preferred_username` are actually used in this
application, I removed the unnecessary fields.

**BoatControllerTest — repeated inline test data:**
AI repeated the same `List.of(new BoatRecord(...))` inline in multiple tests.
I refactored it into a shared helper method for readability and to make
future changes easier.

**Additional test scenarios:**
AI only covered the happy path. I defined and added the missing edge cases
myself: pagination with 0 records, pagination spanning 2 pages, and database
error propagation returning HTTP 500.

**Small changes done directly without AI:**
Adding a missing field, correcting a log message, extracting a method into
a smaller function, fixing a minor CSS issue — these were handled directly
without involving AI to keep things efficient and avoid unnecessary token usage.