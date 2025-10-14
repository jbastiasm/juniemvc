# Project Improvement Plan: Create Beer Order

This plan translates requirements in prompts/create-beer-order/requirements.md into a prioritized, actionable roadmap. It adheres to the provided Spring Boot Guidelines (constructor injection, package‑private components, DTOs, transactions, OSIV off, REST conventions).

## 1. Domain Model Design (Entities) [Priority: P0]
- Define BeerOrder entity
  - Annotations: @Entity, @Table("beer_order"); Lombok: @Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor, @ToString(exclude = "lines")
  - Fields: id (Identity), version, customerRef, paymentAmount (BigDecimal), status (String), createdDate (@CreationTimestamp), updateDate (@UpdateTimestamp), lines (List<BeerOrderLine>)
  - Relationships: @OneToMany(mappedBy="beerOrder", cascade=ALL, orphanRemoval=true, fetch=LAZY)
  - Helpers: addLine/removeLine to manage both sides
  - Rules: exclude collections from equals/hashCode; keep LAZY
- Define BeerOrderLine entity
  - Annotations: @Entity, @Table("beer_order_line"); Lombok: @Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor
  - Fields: id (Identity), version, orderQuantity, quantityAllocated, status, createdDate (@CreationTimestamp), updateDate (@UpdateTimestamp)
  - Relationships: ManyToOne LAZY to BeerOrder with @JoinColumn(name="beer_order_id", nullable=false, foreignKey name "fk_beer_order_line_order"); ManyToOne LAZY to Beer with @JoinColumn(name="beer_id", nullable=false, foreignKey name "fk_beer_order_line_beer")
- DDL conventions
  - Tables: beer (existing), beer_order, beer_order_line
  - FKs: beer_order_line.beer_order_id -> beer_order.id; beer_order_line.beer_id -> beer.id
  - Indexes: beer_order_line(beer_order_id), beer_order_line(beer_id)
- Acceptance checks
  - Persisting BeerOrder cascades to lines; orphan removal works; fetch type remains LAZY.

## 2. Repositories [Priority: P0]
- Create BeerOrderRepository extends JpaRepository<BeerOrder, Integer>
  - Add finder: List<BeerOrder> findByCustomerRef(String customerRef)
  - Optional: @EntityGraph(attributePaths={"lines","lines.beer"}) for read endpoints needing full graph
- Create BeerOrderLineRepository extends JpaRepository<BeerOrderLine, Integer>
- Reuse existing BeerRepository

## 3. Service Layer (Transactional Boundary) [Priority: P0]
- Create package-private @Service BeerOrderService using constructor injection for repositories
- Methods
  - @Transactional BeerOrder placeOrder(BeerOrder draft)
    - Validates presence of lines and referenced beers (basic sanity checks)
    - Saves BeerOrder (cascade persists lines)
  - @Transactional(readOnly=true) List<BeerOrder> getOrdersForCustomer(String customerRef)
- Logging via SLF4J; no System.out.

## 4. DTOs and Mapping [Priority: P1]
- Create DTOs under org.example.juniemvc.models
  - BeerOrderLineDTO: beerId, orderQuantity, quantityAllocated (optional in requests), status (optional)
  - BeerOrderDTO: id, customerRef, paymentAmount, status, createdDate, updateDate, lines: List<BeerOrderLineDTO>
- Create MapStruct mappers in org.example.juniemvc.mappers
  - BeerOrderMapper (entity<->DTO), BeerOrderLineMapper
  - For requests: map beerId to Beer reference (id-only) without fetching; services perform validation
- Validation annotations on request DTOs: @NotBlank customerRef, @NotNull paymentAmount, @Positive orderQuantity, @NotNull beerId

## 5. REST Controller [Priority: P1]
- Package-private @RestController under org.example.juniemvc.controllers
- Base path: /api/v1/beer-orders
- Endpoints
  - POST /api/v1/beer-orders: accepts BeerOrderDTO request, validates, invokes service.placeOrder, returns 201 Created with Location and response DTO
  - GET /api/v1/beer-orders?customerRef=...: returns orders list (DTOs) with 200 OK
- ResponseEntity usage with explicit status codes; JSON top-level object, camelCase fields

## 6. Configuration and OSIV [Priority: P0]
- Ensure spring.jpa.open-in-view=false in application.properties
- If not present, add property and verify no lazy loading in views; ensure repositories/service methods fetch what they need (EntityGraph or dedicated queries)

## 7. Testing Strategy [Priority: P0/P1]
- Unit tests (P0)
  - JPA mapping tests using @DataJpaTest: 
    - Saving BeerOrder cascades to BeerOrderLine (verify FKs)
    - Removing a line and re-saving deletes the line (orphanRemoval)
    - LAZY behavior verified within @Transactional context
  - Service tests: placeOrder persists lines; query by customerRef returns expected results
- Integration tests (P1)
  - @SpringBootTest(webEnvironment = RANDOM_PORT) for controller
  - POST/GET roundtrip with OSIV disabled
- Use Testcontainers if database differs from H2; otherwise H2 for unit tests is acceptable for this task

## 8. Error Handling & Validation [Priority: P1]
- Implement @RestControllerAdvice GlobalExceptionHandler
  - Handle MethodArgumentNotValidException, ConstraintViolationException, EntityNotFound, and generic Exception
  - Return ProblemDetails-like structure with consistent fields (type, title, status, detail, instance)
- Service-level guards: 
  - Validate that each line references an existing Beer id; if not, return 400 via exception handler

## 9. Documentation and Developer Experience [Priority: P2]
- Update README or prompts documentation to include new endpoints, DTO shapes, and example payloads
- Add JavaDoc on service methods and mappers

## 10. Future Enhancements (Backlog) [Priority: P3]
- Convert status fields to enums with dedicated converters
- Add pagination to GET /beer-orders
- Add search/filter by status and date ranges
- Introduce Flyway migrations for beer_order and beer_order_line tables
- Add optimistic locking conflict tests

## Work Breakdown and Milestones
- Milestone M1 (P0): Entities, repositories, service, OSIV setting, core unit tests
- Milestone M2 (P1): DTOs, mappers, controller, validation, integration tests, exception handling
- Milestone M3 (P2/P3): Docs, enhancements and backlog grooming

## Acceptance Criteria Mapping
- AC1–AC7 in requirements.md directly satisfied by M1 and M2 deliverables.
- Ensure code follows guidelines: constructor injection, package-private components, DTO separation, transactional boundaries, lazy fetching, no System.out, consistent logging.
