# Detailed Task List for Introducing DTOs and Mapper Layer

Note: Use [ ] to mark pending tasks and [x] when completed.

1. Prerequisites and Repository Review
   1.1. [ ] Verify Lombok dependency exists in pom.xml and annotation processing is enabled.
   1.2. [ ] Add MapStruct dependencies (org.mapstruct:mapstruct and org.mapstruct:mapstruct-processor) if missing.
   1.3. [ ] Ensure maven-compiler-plugin is configured for annotation processing (Lombok + MapStruct).
   1.4. [ ] Confirm current endpoints under /api/beers and identify places exposing entities in Controller/Service.

2. Add BeerDTO
   2.1. [ ] Create class org.example.juniemvc.models.BeerDTO.
   2.2. [ ] Add fields: id, version, beerName, beerStyle, upc, quantityOnHand, price, createdDate, updateDate.
   2.3. [ ] Add Lombok annotations: @Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor.
   2.4. [ ] Keep class public and compile to ensure no syntax issues.

3. Add MapStruct BeerMapper
   3.1. [ ] Create interface org.example.juniemvc.mappers.BeerMapper with @Mapper(componentModel = "spring").
   3.2. [ ] Define mappings: BeerDTO toDto(Beer entity), Beer toEntity(BeerDTO dto).
   3.3. [ ] Configure DTO -> Entity mapping to ignore: id, createdDate, updateDate.
   3.4. [ ] Add method: void updateEntityFromDto(BeerDTO dto, @MappingTarget Beer entity) with ignores for id/createdDate/updateDate.
   3.5. [ ] Build project to confirm MapStruct generates the mapper implementation.

4. Refactor Service Interface to DTOs
   4.1. [ ] Update BeerService signatures to use BeerDTO (findAll, findById, create, update, deleteById).
   4.2. [ ] Adjust imports and JavaDoc/comments accordingly.

5. Refactor Service Implementation
   5.1. [ ] Inject BeerRepository and BeerMapper via constructor (final fields).
   5.2. [ ] Implement findAll(): map entities to DTOs via mapper.toDto.
   5.3. [ ] Implement findById(id): map Optional<Beer> to Optional<BeerDTO> via mapper.toDto.
   5.4. [ ] Implement create(dto): mapper.toEntity(dto), set entity.id = null, save, mapper.toDto(saved).
   5.5. [ ] Implement update(id, dto): load entity; if present, mapper.updateEntityFromDto(dto, entity), save, mapper.toDto(saved).
   5.6. [ ] Ensure id/createdDate/updateDate are never altered by DTO input.

6. Refactor Controller to Use DTOs
   6.1. [ ] Change listAll() to return List<BeerDTO>.
   6.2. [ ] Change getById(Integer id) to return BeerDTO.
   6.3. [ ] Change create(@RequestBody BeerDTO) to return ResponseEntity<BeerDTO> with 201 and Location header /api/beers/{id}.
   6.4. [ ] Change update(@PathVariable Integer id, @RequestBody BeerDTO) to return BeerDTO.
   6.5. [ ] Keep delete endpoint behavior; return 204 on success, 404 when not found.
   6.6. [ ] Preserve ResponseStatusException for 404 cases.

7. Maven Dependencies and Build Config
   7.1. [ ] Add mapstruct.version property (e.g., 1.5.x) to pom.xml if not present.
   7.2. [ ] Add org.mapstruct:mapstruct (implementation) and org.mapstruct:mapstruct-processor (annotationProcessor).
   7.3. [ ] Ensure Lombok dependencies and annotationProcessor are defined.
   7.4. [ ] Verify maven-compiler-plugin source/target compatibility and annotation processing.

8. Update Tests
   8.1. [ ] Update controller tests to use BeerDTO JSON shape for requests and expected responses.
   8.2. [ ] Update service tests to interact with BeerDTO instead of Beer entity.
   8.3. [ ] Keep repository tests unchanged (still operate on entity).
   8.4. [ ] (Optional) Add a unit test for BeerMapper (entity <-> DTO round-trip checks).

9. Build and Verification
   9.1. [ ] Run mvn -q -DskipTests=false clean test to compile and execute tests.
   9.2. [ ] Resolve any MapStruct/Lombok annotation processing errors.
   9.3. [ ] Start the application and perform manual smoke tests (POST/GET/PUT/DELETE) to ensure DTO payloads are accepted/returned.
   9.4. [ ] Confirm createdDate/updateDate are not writable via API and are present when returned.

10. Acceptance Checklist (Final)
   10.1. [ ] BeerDTO exists with required fields and Lombok annotations.
   10.2. [ ] BeerMapper exists with correct mappings and ignored fields.
   10.3. [ ] BeerService and BeerServiceImpl operate on DTOs and use the mapper for entity conversions.
   10.4. [ ] BeerController uses DTOs for all endpoints.
   10.5. [ ] MapStruct dependencies configured; project builds successfully.
   10.6. [ ] Tests updated and passing (controller, service; repository unaffected).
   10.7. [ ] Manual smoke tests confirm expected behavior.
