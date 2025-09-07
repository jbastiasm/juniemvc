# Juniemvc – Developer Quick Guidelines

Last updated: 2025-09-07

## 1) Tech stack at a glance
- Runtime: Java 21
- Framework: Spring Boot 3.5.x (Web, Validation, Data JPA)
- DB: H2 (in-memory, runtime scope). Flyway dependency is present; add migrations in `src/main/resources/db/migration` if needed.
- Mapping: MapStruct 1.6.x (default component model = spring)
- Boilerplate: Lombok
- Build/Test: Maven (Surefire for unit tests)

Key file: `pom.xml` configures MapStruct processor and Spring Boot plugin.

## 2) Project structure
- `src/main/java/org/example/juniemvc`
  - `JuniemvcApplication.java` – app entry point
  - `controllers/` – REST controllers (e.g., `BeerController`)
  - `service/` – service interfaces and implementations (`BeerService`, `BeerServiceImpl`)
  - `repositories/` – Spring Data JPA repositories (`BeerRepository`)
  - `entities/` – JPA entities (`Beer`)
  - Consider adding `dto/` and `mappers/` if exposing DTOs with MapStruct.
- `src/main/resources`
  - `application.properties` – app and datasource config
  - `db/migration` – Flyway SQL migrations (create this path if you start using Flyway)
- `src/test/java/org/example/juniemvc`
  - `controllers/`, `service/`, `repositories/` – layered tests

Suggested conventions when adding features:
- Controller -> Service -> Repository -> Entity/DTO. Keep controllers thin; put business logic in services.
- Create interfaces in `service` and put `Impl` classes alongside.
- For mapping, define `@Mapper(componentModel = "spring")` in `mappers/` and inject in services.

## 3) How to run
- Run app: `./mvnw spring-boot:run`
- Build jar: `./mvnw -DskipTests package` (or without -DskipTests to run tests)
- Run packaged jar: `java -jar target/juniemvc-0.0.1-SNAPSHOT.jar`

Profiles and DB:
- Default uses H2 (memory). For real DBs, add properties or profiles in `application-<profile>.properties` and run with `-Dspring-boot.run.profiles=<profile>`.

## 4) How to run tests
- All tests: `./mvnw test`
- Single test class: `./mvnw -Dtest=org.example.juniemvc.controllers.BeerControllerTests test`
- Pattern: `./mvnw -Dtest=*RepositoryTests test`
- Fail fast (optional): `./mvnw -DfailIfNoTests=false -DtrimStackTrace=false test`

Testing tips:
- Controller tests: use `@WebMvcTest` or `MockMvc`.
- Service tests: mock repositories with Mockito, or use `@SpringBootTest` for integration.
- Repository tests: leverage H2 + `@DataJpaTest` for slice tests.

## 5) Executing scripts and useful tasks
- Maven commands are the primary way to execute tasks/scripts.
  - Format/inspect: add plugins as needed (e.g., Spotless/Checkstyle).
  - DB migrations: place SQL files in `src/main/resources/db/migration` (e.g., `V1__init.sql`). Flyway will auto-run at startup when configured.
- Ad-hoc code execution: create a `CommandLineRunner` bean in `JuniemvcApplication` or a dedicated `@Component` for one-off scripts; guard with a Spring profile so it doesn’t run in prod/tests.

Example CommandLineRunner:
```java
@Bean
@Profile("seed")
CommandLineRunner seed(BeerRepository repo) {
  return args -> {
    // seed sample data
  };
}
```
Run with: `./mvnw spring-boot:run -Dspring-boot.run.profiles=seed`

## 6) Best practices (concise)
- Keep controllers thin; validate inputs with `jakarta.validation` annotations and `@Valid`.
- Use DTOs for external APIs; avoid exposing entities directly.
- Transactions: annotate service methods that modify data with `@Transactional`.
- MapStruct: keep mappings in dedicated interfaces; avoid complex logic in mappers.
- Lombok: prefer `@Getter @Setter @Builder @RequiredArgsConstructor`; be mindful of equals/hashCode for JPA entities (use ID carefully).
- Error handling: add a `@ControllerAdvice` with meaningful responses.
- Config: centralize in `application.properties`; externalize secrets via environment variables.
- Tests: follow the same package structure as main code; name tests with clear intent; keep unit tests fast and deterministic.
- CI: run `./mvnw -q -e -DskipTests=false verify` in pipelines.

## 7) Common paths and ports
- Default server port: 8080 (override with `server.port`)
- Typical API path example: `/api/beers`

## 8) Where to start
1. Run tests: `./mvnw test`
2. Start the app: `./mvnw spring-boot:run`
3. Add your feature following the structure in section 2.
4. Add/adjust tests under the matching test package.

Keep this document short and practical. Update it when the stack or conventions change.