---
name: java-best-practices
description: Apply Java Spring Boot best practices — layered architecture,
  package structure, code quality, naming conventions. Use when creating
  any new class, reviewing or refactoring code.
  Triggers on new class, feature, architecture, refactor, review.
allowed-tools: ["read", "edit", "search/codebase"]
---

# Java Spring Boot — best practices

## Layered architecture (strict)
Call hierarchy — never skip or reverse:

Controller → Service → Repository

- Controllers: HTTP only — parse request, call service, return
  ResponseEntity. Zero business logic.
- Services: all business logic. Never call another service directly
  — extract shared logic to a private method or helper class.
- Repositories: persistence only. No business logic, no HTTP concerns.
- Entities never leave the service layer — always map to DTO/Record
  via MapStruct before returning from a controller.

## Package structure
com.boatapp.backend/
controller/   ← REST controllers + GlobalExceptionHandler
service/      ← business logic
repository/   ← Spring Data repositories
entity/       ← JPA entities
dto/          ← Records (Request/Response)
mapper/       ← MapStruct interfaces
security/     ← SecurityConfig, JWT filter
config/       ← OpenAPI, app beans

Never add packages outside this structure without justification.

## Code quality
- Constructor injection always — never @Autowired on fields
- All injected dependencies must be final:
  private final IBoatService boatService;   ← inject the interface, not the impl
- Single Responsibility — one class, one reason to change
- Max 25 lines per method if possible — extract if longer
- No code duplication — extract to private method or utility
- Never return null — use Optional<T> or throw a named exception
- All public service methods must have a Javadoc comment
- Service classes: annotate with @Transactional(readOnly = true) at class level,
  override with @Transactional on write methods

## Logging — @Slf4j (Lombok)
Add @Slf4j on every service class and on GlobalExceptionHandler.
Never add it on controllers — HTTP details are already logged by Spring.

Rules:
- log.debug() — method entry with input ids in services (dev only, cheap)
- log.info()  — significant business events: entity created, deleted, status changed
- log.warn()  — expected but notable failures: entity not found, validation rejected
- log.error() — unexpected exceptions only, always pass the exception as last arg

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
      log.error("Unexpected error: {}", ex.getMessage(), ex);
      ...
  }

In services, log warn before throwing a not-found exception:
  log.warn("Boat not found with id: {}", id);
  throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Boat not found with id: " + id);

Never log: passwords, tokens, full stack traces at warn level, personal data.

## Exception handling
- Throw ResponseStatusException for HTTP-mapped errors (404, 400, 409…)
  — it carries the status and reason, and GlobalExceptionHandler handles it uniformly
- For complex domains, create named exceptions extending RuntimeException:
  class BoatNotFoundException extends RuntimeException { }
- Let all exceptions bubble up to GlobalExceptionHandler
- Never swallow exceptions with an empty catch block

## Configuration and constants
- No hardcoded strings or magic numbers in business logic
- Use @Value or @ConfigurationProperties for all config values
- Group related constants as static final fields in the relevant class

## Naming conventions
- Classes: PascalCase descriptive noun — BoatService, not BoatMgr
- Methods: camelCase verb-first — findById, createBoat, deleteById
- Variables: camelCase, no abbreviations — boatRepository not repo
- Constants: UPPER_SNAKE_CASE
- Tests: suffix Test (unit) or IT (integration)
- Service interfaces: prefix with I — IBoatService, not BoatServiceInterface
