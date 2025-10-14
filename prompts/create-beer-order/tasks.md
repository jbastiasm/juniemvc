# Task List: Create Beer Order

Enumerated tasks derived from prompts/create-beer-order/plan.md. Each item includes a checkbox [ ] to mark completion.

1. [ ] Domain Model Design (Entities) — Priority P0
   1.1. [x] Create BeerOrder entity with fields: id, version, customerRef, paymentAmount, status, createdDate, updateDate, lines.
   1.2. [x] Configure BeerOrder annotations: @Entity, @Table("beer_order"), Lombok (@Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor, @ToString excluding lines).
   1.3. [x] Implement BeerOrder relationships: @OneToMany(mappedBy = "beerOrder", cascade = ALL, orphanRemoval = true, fetch = LAZY).
   1.4. [x] Add helper methods in BeerOrder: addLine(BeerOrderLine) and removeLine(BeerOrderLine) maintaining both sides of association.
   1.5. [x] Exclude collections from equals/hashCode for BeerOrder; keep LAZY fetch.
   1.6. [x] Create BeerOrderLine entity with fields: id, version, orderQuantity, quantityAllocated, status, createdDate, updateDate.
   1.7. [x] Configure BeerOrderLine annotations: @Entity, @Table("beer_order_line"), Lombok (@Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor).
   1.8. [x] Implement BeerOrderLine relationships: ManyToOne LAZY to BeerOrder with @JoinColumn(name = "beer_order_id", nullable = false, foreignKey name "fk_beer_order_line_order"); ManyToOne LAZY to Beer with @JoinColumn(name = "beer_id", nullable = false, foreignKey name "fk_beer_order_line_beer").
   1.9. [x] Define DDL conventions: ensure tables beer_order and beer_order_line are created; set FKs; add indexes on beer_order_id and beer_id.
   1.10. [ ] Acceptance checks: cascading persists lines; orphan removal works; fetch type remains LAZY.

2. [ ] Repositories — Priority P0
   2.1. [x] Create BeerOrderRepository extends JpaRepository<BeerOrder, Integer>.
   2.2. [x] Add finder to BeerOrderRepository: List<BeerOrder> findByCustomerRef(String customerRef).
   2.3. [x] Optionally add @EntityGraph(attributePaths = {"lines", "lines.beer"}) for read endpoints.
   2.4. [x] Create BeerOrderLineRepository extends JpaRepository<BeerOrderLine, Integer>.

3. [ ] Service Layer (Transactional Boundary) — Priority P0
   3.1. [x] Implement package-private @Service BeerOrderService with constructor injection for repositories.
   3.2. [x] Implement @Transactional BeerOrder placeOrder(BeerOrder draft): validate presence of lines and referenced beers; save BeerOrder (cascade persists lines).
   3.3. [x] Implement @Transactional(readOnly = true) List<BeerOrder> getOrdersForCustomer(String customerRef).
   3.4. [x] Add SLF4J logging; remove any System.out usage.

4. [ ] DTOs and Mapping — Priority P1
   4.1. [ ] Create DTOs: BeerOrderLineDTO (beerId, orderQuantity, quantityAllocated optional, status optional) and BeerOrderDTO (id, customerRef, paymentAmount, status, createdDate, updateDate, lines).
   4.2. [ ] Create MapStruct mappers: BeerOrderMapper and BeerOrderLineMapper (entity <-> DTO).
   4.3. [ ] For requests, map beerId to Beer reference (id-only) without fetching; let services perform validation.
   4.4. [ ] Add validation annotations: @NotBlank customerRef, @NotNull paymentAmount, @Positive orderQuantity, @NotNull beerId.

5. [ ] REST Controller — Priority P1
   5.1. [ ] Create package-private @RestController under controllers with base path /api/v1/beer-orders.
   5.2. [ ] Implement POST /api/v1/beer-orders: accept BeerOrderDTO, validate, call service.placeOrder, return 201 Created with Location and response DTO.
   5.3. [ ] Implement GET /api/v1/beer-orders?customerRef=...: return list of BeerOrderDTO with 200 OK.
   5.4. [ ] Ensure ResponseEntity usage with explicit status codes; JSON top-level object; consistent camelCase.

6. [ ] Configuration and OSIV — Priority P0
   6.1. [x] Ensure spring.jpa.open-in-view=false is set in application.properties.
   6.2. [ ] Verify no lazy loading occurs in views; fetch needed associations via EntityGraph or dedicated queries.

7. [ ] Testing Strategy — Priority P0/P1
   7.1. [ ] JPA mapping tests (@DataJpaTest): saving BeerOrder cascades to BeerOrderLine and verifies FKs.
   7.2. [ ] JPA mapping tests: removing a line and re-saving deletes the line (orphanRemoval).
   7.3. [ ] JPA mapping tests: verify LAZY behavior within @Transactional context.
   7.4. [ ] Service tests: placeOrder persists lines; query by customerRef returns expected results.
   7.5. [ ] Integration tests (@SpringBootTest RANDOM_PORT): POST/GET roundtrip with OSIV disabled.
   7.6. [ ] Decide on Testcontainers vs H2; use H2 for unit tests unless production DB differs.

8. [ ] Error Handling & Validation — Priority P1
   8.1. [ ] Implement @RestControllerAdvice GlobalExceptionHandler.
   8.2. [ ] Handle MethodArgumentNotValidException, ConstraintViolationException, EntityNotFound, and generic Exception.
   8.3. [ ] Return ProblemDetails-like error structure (type, title, status, detail, instance).
   8.4. [ ] Service-level guards: validate each line references an existing Beer id; return 400 via exception handler if invalid.

9. [ ] Documentation and Developer Experience — Priority P2
   9.1. [ ] Update README or prompts documentation with new endpoints, DTO shapes, and example payloads.
   9.2. [ ] Add JavaDoc on service methods and mappers.

10. [ ] Future Enhancements (Backlog) — Priority P3
   10.1. [ ] Convert status fields to enums with converters.
   10.2. [ ] Add pagination to GET /api/v1/beer-orders.
   10.3. [ ] Add search/filter by status and date ranges.
   10.4. [ ] Introduce Flyway migrations for beer_order and beer_order_line tables.
   10.5. [ ] Add optimistic locking conflict tests.

11. [ ] Milestones and Acceptance
   11.1. [ ] Milestone M1 (P0): Entities, repositories, service, OSIV setting, core unit tests completed.
   11.2. [ ] Milestone M2 (P1): DTOs, mappers, controller, validation, integration tests, exception handling completed.
   11.3. [ ] Milestone M3 (P2/P3): Docs and backlog enhancements completed.
   11.4. [ ] Verify Acceptance Criteria AC1–AC7 in requirements.md are satisfied by M1 and M2 deliverables.
   11.5. [ ] Ensure code follows guidelines: constructor injection, package-private components, DTO separation, transactional boundaries, lazy fetching, no System.out, consistent logging.
