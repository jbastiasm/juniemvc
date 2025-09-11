# Plan to Introduce DTOs and Mapper Layer for Beer API

## 1. Scope and Objectives
- Implement BeerDTO and MapStruct-based BeerMapper.
- Refactor Service and Controller layers to use DTOs exclusively at their boundaries.
- Keep Repository and Entity as-is; mapping occurs in Service layer.
- Ensure build works with MapStruct + Lombok; update tests accordingly.

## 2. Prerequisites and Repository Review
- Verify Lombok is present and annotation processing enabled in Maven.
- Add MapStruct dependencies (runtime + annotation processor) if missing.
- Confirm existing endpoints under /api/beers and service structure.
- Note: Current code exposes entity Beer in Controller and Service.

## 3. Step-by-step Implementation Plan

### 3.1. Add DTO
- Package: org.example.juniemvc.models
- Class: BeerDTO
- Fields mirror Beer entity: id, version, beerName, beerStyle, upc, quantityOnHand, price, createdDate, updateDate.
- Lombok annotations: @Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor.
- Keep class public (needed for JSON serialization and tests).

### 3.2. Add MapStruct Mapper
- Package: org.example.juniemvc.mappers
- Interface: BeerMapper
- Annotations: @Mapper(componentModel = "spring")
- Methods:
  - BeerDTO toDto(Beer entity)
  - Beer toEntity(BeerDTO dto)
  - void updateEntityFromDto(BeerDTO dto, @MappingTarget Beer entity)
- Mapping rules:
  - When mapping DTO -> Entity, ignore: id, createdDate, updateDate always.
  - Consider ignoring version on create; allow update via updateEntityFromDto (MapStruct will copy non-null fields unless specified).
- Provide explicit @Mapping(target = "id", ignore = true), etc., and an @BeanMapping(ignoreByDefault = false) where appropriate.

### 3.3. Update Service Interface
- Change signatures to DTOs:
  - List<BeerDTO> findAll();
  - Optional<BeerDTO> findById(Integer id);
  - BeerDTO create(BeerDTO beer);
  - Optional<BeerDTO> update(Integer id, BeerDTO beer);
  - boolean deleteById(Integer id);

### 3.4. Update Service Implementation
- Inject BeerRepository and BeerMapper via constructor (final fields).
- findAll(): repository.findAll() -> map each to DTO via mapper.toDto.
- findById(id): repository.findById(id) -> map to DTO.
- create(dto):
  - entity = mapper.toEntity(dto)
  - entity.setId(null)
  - save -> mapper.toDto(saved)
- update(id, dto):
  - load entity; if present, mapper.updateEntityFromDto(dto, entity) then save and return mapper.toDto.
  - Ensure id/createdDate/updateDate are not altered by mapper config.
- deleteById(id): unchanged logic.

### 3.5. Refactor Controller
- Change method signatures and bodies to use BeerDTO for request/response.
- Endpoints and status codes remain same.
- POST: return ResponseEntity.created(URI.of("/api/beers/{id}")) with DTO body.
- Keep ResponseStatusException handling for 404.

### 3.6. Maven Dependencies and Build Configuration
- In pom.xml:
  - Add MapStruct version property (e.g., mapstruct.version = 1.5.x or latest stable compatible).
  - Dependencies:
    - implementation: org.mapstruct:mapstruct
    - annotationProcessor: org.mapstruct:mapstruct-processor
  - Ensure Lombok dependencies exist and annotationProcessor for Lombok configured.
  - Ensure maven-compiler-plugin has annotation processing enabled and correct source/target.

### 3.7. Tests Update Plan
- Controller tests: adjust JSON payloads and assertions to BeerDTO shape; URLs and statuses remain.
- Service tests: interact with DTOs instead of entities; mock repository if needed or keep existing tests updated.
- Repository tests: unchanged (operate on Entity directly).
- Optional: add a simple unit test for BeerMapper (entity <-> DTO round-trip for key fields).

### 3.8. Validation and Error Handling (Future-ready)
- Leave placeholders to add Jakarta Validation annotations on BeerDTO.
- Keep ResponseStatusException for now; consider introducing @RestControllerAdvice in a follow-up story.

### 3.9. Build and Verification Steps
- mvn -q -DskipTests=false clean test
- If MapStruct errors: verify annotation processor configuration and imports.
- Run the app and manual smoke test endpoints with curl/Postman to ensure DTO JSON is returned and accepted.

### 3.10. Migration/Compatibility Notes
- URL paths remain the same; JSON schema changes to DTO; communicate to clients.
- Entities are no longer exposed by controller methods.
- Ensure createdDate/updateDate are not writable via API.

## 4. Work Breakdown and Effort Estimate
- DTO + Mapper: 1h (including MapStruct setup)
- Service refactor: 1h
- Controller refactor: 0.5h
- Test updates: 1–2h depending on coverage
- Build and fix: 0.5–1h

## 5. Risks and Mitigations
- MapStruct compilation issues: pin compatible versions; ensure annotation processors configured.
- Serialization differences (e.g., LocalDateTime format): rely on Spring Boot default; adjust only if tests fail.
- Tests tightly coupled to entity fields: refactor to DTO assertions.

## 6. Acceptance Checklist
- [ ] BeerDTO exists with required fields and Lombok annotations.
- [ ] BeerMapper exists with correct mappings and ignored fields.
- [ ] BeerService and BeerServiceImpl refactored to DTOs and mapper used.
- [ ] BeerController uses DTOs for all endpoints.
- [ ] Dependencies for MapStruct added; project builds successfully.
- [ ] Tests updated and passing (controller, service; repository unaffected).
- [ ] Manual smoke tests confirm expected behavior.
