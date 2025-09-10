# Add DTOs and Mapper Layer for Beer API

## Overview
Refactor the current Beer API to introduce Data Transfer Objects (DTOs) and a dedicated mapping layer. The goal is to decouple the web layer from the JPA persistence layer, improve API stability, and align with the provided Spring Boot guidelines (constructor injection, DTO separation, validation, and REST best practices).

The changes must be backward compatible only at the URL level; the JSON schema will change to use DTOs.

## Goals
- Introduce BeerDTO for all controller inputs and outputs (no entities in controllers).
- Add MapStruct-based mappers to convert between entity and DTO.
- Keep service layer boundaries clear and accept/return DTOs (service becomes DTO-facing; repository remains entity-facing).
- Ignore server-managed fields when mapping from DTO to Entity.
- Prepare for validation, error handling, and test updates.

## Out of Scope
- Database schema changes.
- Authentication/authorization.
- Non-Beer resources.

## Technical Requirements

### 1. DTO Definition
- Package: `org.example.juniemvc.models` (note: "models" not "m̀odels").
- Class name: `BeerDTO`.
- Fields:
  - id: Integer (nullable; server-generated)
  - version: Integer (nullable; server-managed)
  - beerName: String
  - beerStyle: String
  - upc: String
  - quantityOnHand: Integer
  - price: BigDecimal
  - createdDate: LocalDateTime (nullable; server-managed)
  - updateDate: LocalDateTime (nullable; server-managed)
- Lombok annotations: `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- Future-ready: Leave room to add Jakarta Validation annotations (not mandatory in this change set).

### 2. MapStruct Mapper
- Package: `org.example.juniemvc.mappers`.
- Interface: `BeerMapper`.
- Add `@Mapper(componentModel = "spring")`.
- Methods:
  - `BeerDTO toDto(Beer entity);`
  - `Beer toEntity(BeerDTO dto);`
- Mapping rules when converting from DTO to Entity:
  - Ignore `id`, `createdDate`, and `updateDate` (server manages these).
  - Optionally ignore `version` on create; allow mapping for update if present.
- Provide an `@BeanMapping` or `@Mapping(target = ..., ignore = true)` as appropriate.
- Consider an additional method for partial updates:
  - `void updateEntityFromDto(BeerDTO dto, @MappingTarget Beer entity);` (ignore id/createdDate/updateDate).

### 3. Service Layer Changes
- Change `BeerService` signatures to accept/return `BeerDTO` instead of `Beer`:
  - `List<BeerDTO> findAll();`
  - `Optional<BeerDTO> findById(Integer id);`
  - `BeerDTO create(BeerDTO beer);`
  - `Optional<BeerDTO> update(Integer id, BeerDTO beer);`
  - `boolean deleteById(Integer id);`
- Update `BeerServiceImpl` accordingly:
  - Use `BeerMapper` to map between DTOs and entities.
  - On `create`: ensure entity.id is `null` before save.
  - On `update`: load entity, copy allowed fields from DTO (do not change id/createdDate/updateDate; version only if you decide to support optimistic locking via DTO).

### 4. Controller Changes
- Continue to use constructor injection.
- Change request/response types to use `BeerDTO`.
- Endpoints remain under `/api/beers`.
- Behavior:
  - POST returns 201 Created with Location header `/api/beers/{id}` and body as `BeerDTO`.
  - GET/PUT/DELETE semantics unchanged; return `404 Not Found` when applicable.

### 5. Error Handling (Minimal)
- Keep existing `ResponseStatusException` usage.
- A future task can introduce `@RestControllerAdvice` for consistent ProblemDetails, but not required now.

### 6. Logging
- Do not introduce `System.out.println`; use framework logging if needed. No sensitive data in logs.

### 7. Build and Dependencies
- Add MapStruct and Lombok dependencies if not already present in `pom.xml`:
  - Lombok (provided already likely).
  - MapStruct runtime and annotation processor.
- Ensure annotation processing is enabled for both Lombok and MapStruct in Maven compiler plugin.

### 8. Tests
- Update existing controller and service tests to work with `BeerDTO` JSON shapes.
- Maintain endpoint URLs and HTTP status assertions.
- If convenient, add simple mapper unit tests (optional).

## Acceptance Criteria
1. A new class `org.example.juniemvc.models.BeerDTO` exists with Lombok annotations and fields listed above.
2. A new mapper `org.example.juniemvc.mappers.BeerMapper` exists using MapStruct with methods `toDto`, `toEntity`, and (optionally) `updateEntityFromDto`.
3. When mapping from DTO to Entity, the properties `id`, `createdDate`, and `updateDate` are ignored.
4. `BeerService` and `BeerServiceImpl` operate on `BeerDTO` and use the mapper to interact with `Beer` entities and the repository.
5. `BeerController` request/response payloads use `BeerDTO` exclusively; no entity classes are exposed by controller methods.
6. All existing tests compile and pass after being updated to the new DTO-based API, or new equivalent tests are provided if the previous ones relied on entity shapes.
7. The application builds successfully with MapStruct processing enabled.

## Implementation Notes
- Constructor injection only; no field/setter injection.
- Keep classes as package-private where feasible, except where public is required by frameworks/tests.
- Consider future addition of Jakarta Validation annotations on BeerDTO (e.g., `@NotBlank` for beerName, `@Positive` for price, etc.).
- If optimistic locking via `version` is used, clarify behavior in updates; otherwise you may ignore `version` in DTO-to-entity mapping during updates until a dedicated story.

## Migration Steps
1. Create BeerDTO and BeerMapper.
2. Refactor service interface and implementation to use DTOs and mapper.
3. Refactor controller to use DTOs.
4. Adjust tests to the new DTO JSON.
5. Add/update dependencies in pom.xml for MapStruct (if missing).
6. Run the build and tests; fix any mapping or serialization issues.

## Glossary
- DTO: Data Transfer Object, a plain object used to communicate across layers (web/service) without exposing persistence entities.
- MapStruct: A compile-time code generator for bean mappings.
