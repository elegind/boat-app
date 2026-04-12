---
name: java-testing
description: Write unit and integration tests for Java Spring Boot code.
  Use whenever asked to write, add, fix or review any test — unit tests
  with JUnit 5 and Mockito, integration tests with TestContainers.
  Triggers on test, JUnit, Mockito, TestContainers, assert, mock, verify.
allowed-tools: ["read", "edit", "search/codebase"]
---

# Java Testing

## Naming convention (non-negotiable)
Pattern: `[methodtested]_should_[expectedBehavior]_when_[condition]`

findById_should_returnBoat_when_idExists()
findById_should_throwNotFoundException_when_idDoesNotExist()
create_should_return400_when_nameIsNull()
findAll_should_returnEmptyList_when_noBoatsExist()

Never: test_something(), testMethod1(), givenX_thenY(), should_doX_when_Y() (missing method prefix)

## AAA structure — mandatory on every test
Every test has exactly three labeled sections:

@Test
void findById_should_returnBoat_when_idExists() {
    // Arrange
    var boat = new Boat(1L, "Titanic", "Big ship", Instant.now());
    when(boatRepository.findById(1L)).thenReturn(Optional.of(boat));

    // Act
    var result = boatService.findById(1L);

    // Assert
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("Titanic");
}

- Arrange: set up data and mocks only
- Act: call exactly ONE method under test
- Assert: verify outcome only — never call production code again here

## Unit tests
- @ExtendWith(MockitoExtension.class) on the class
- @Mock for dependencies, @InjectMocks for the class under test
- AssertJ (assertThat) for all assertions — never assertEquals/assertTrue
- One logical concept per test — use assertThat().satisfies() for
  multiple fields on the same object
- Never test private methods — test through the public API
- Class name: [Subject]Test — e.g. BoatServiceTest

## Integration tests
- @WebMvcTest → controller layer only, use MockMvc + @MockBean
- @DataJpaTest → repository layer only, loads JPA context
- @SpringBootTest → full application context
- @Testcontainers + @Container for real external services:
  @Container
  static PostgreSQLContainer<?> postgres =
  new PostgreSQLContainer<>("postgres:16-alpine");
- Never use H2 for integration tests if production DB is PostgreSQL
- Class name: [Subject]IT — e.g. BoatControllerIT

## Controller tests (@WebMvcTest)
Always assert status + body + content type together.
Use @MockitoBean (Spring Boot 3.4+) — never the deprecated @MockBean.

mockMvc.perform(get("/api/v1/boats/1"))
.andExpect(status().isOk())
.andExpect(content().contentType(MediaType.APPLICATION_JSON))
.andExpect(jsonPath("$.name").value("Titanic"));

mockMvc.perform(get("/api/v1/boats/999"))
.andExpect(status().isNotFound())
.andExpect(jsonPath("$.status").value(404));

## Coverage expectations — per new class
1. Happy path (valid input, expected output)
2. Edge case (empty list, zero, boundary value)
3. Error path (not found, invalid input, exception thrown)

## What NOT to test
- Lombok-generated getters/setters
- Spring Boot auto-configuration
- Framework internals (JPA, Hibernate)
- Private methods directly