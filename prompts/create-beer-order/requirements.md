# Create Beer Order: JPA Relationships, Services, and API Requirements

## Overview
Model and implement Beer orders based on the provided ERD: BeerOrder —< BeerOrderLine >— Beer. This change introduces two new entities (BeerOrder and BeerOrderLine), their Spring Data repositories, transactional service boundary, and DTO-based API guidance. The implementation must follow the Spring Boot Guidelines provided in the project (constructor injection, package-private Spring components, DTO separation, transactional boundaries, OSIV disabled, REST conventions).

The existing Beer entity is reused as-is; no changes are required to add relationships from Beer to order lines (keep association owned by BeerOrderLine).

## Goals
- Persist Beer orders and their lines using JPA with clear, safe relationships.
- Provide repositories for BeerOrder and BeerOrderLine.
- Add a transactional service for placing and reading orders.
- Keep entities free from recursion/equals pitfalls; prefer LAZY fetching.
- Keep controllers DTO-based (no entity exposure) and ready for validation.

## Out of Scope
- Authentication/authorization.
- Payment processing, shipping workflows, or external system integrations.
- Advanced order state machine; use a simple status field for now.
- Database migration files (Flyway/Liquibase) are recommended but not mandatory in this task.

## Domain Model Requirements

### 1. Beer (existing)
- Location: src/main/java/org/example/juniemvc/entities/Beer.java
- Fields: id, version, beerName, beerStyle, upc, quantityOnHand, price, createdDate, updateDate.
- No changes required; do not add a collection of BeerOrderLine to Beer.

### 2. BeerOrder
- Package/Class: org.example.juniemvc.entities.BeerOrder
- Annotations: @Entity, @Table(name = "beer_order")
- Lombok: @Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor, and optionally @ToString(exclude = "lines")
- Fields:
  - id: Integer, @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  - version: Integer, @Version
  - customerRef: String
  - paymentAmount: BigDecimal
  - status: String (consider enum later: NEW, PAID, SHIPPED)
  - createdDate: LocalDateTime, @CreationTimestamp
  - updateDate: LocalDateTime, @UpdateTimestamp
  - lines: List<BeerOrderLine> (default empty list)
    - @OneToMany(mappedBy = "beerOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
- Utility methods to maintain both sides:
  - addLine(BeerOrderLine line): set line.setBeerOrder(this); add to lines
  - removeLine(BeerOrderLine line): set line.setBeerOrder(null); remove from lines
- Notes:
  - Do not include collections in equals/hashCode.
  - Prefer LAZY fetch (do not switch to EAGER).

### 3. BeerOrderLine
- Package/Class: org.example.juniemvc.entities.BeerOrderLine
- Annotations: @Entity, @Table(name = "beer_order_line")
- Lombok: @Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor
- Fields:
  - id: Integer, @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  - version: Integer, @Version
  - orderQuantity: Integer
  - quantityAllocated: Integer
  - status: String
  - createdDate: LocalDateTime, @CreationTimestamp
  - updateDate: LocalDateTime, @UpdateTimestamp
  - beerOrder: BeerOrder
    - @ManyToOne(fetch = FetchType.LAZY)
    - @JoinColumn(name = "beer_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_beer_order_line_order"))
  - beer: Beer
    - @ManyToOne(fetch = FetchType.LAZY)
    - @JoinColumn(name = "beer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_beer_order_line_beer"))
- Notes:
  - BeerOrderLine owns the association to BeerOrder via beer_order_id.
  - Keep both @ManyToOne as LAZY.

### 4. Table/DDL Conventions
- Tables: beer (existing), beer_order, beer_order_line
- FK columns in beer_order_line: beer_order_id, beer_id
- Optional indexes on beer_order_line: (beer_order_id), (beer_id)

## Repositories
- Package: org.example.juniemvc.repositories
- BeerOrderRepository extends JpaRepository<BeerOrder, Integer>
  - Optionally add an @EntityGraph on queries that require lines and beers loaded: attributePaths = {"lines", "lines.beer"}
  - Example: List<BeerOrder> findByCustomerRef(String customerRef)
- BeerOrderLineRepository extends JpaRepository<BeerOrderLine, Integer>
- BeerRepository already exists (reuse).

## Service Layer
- Package: org.example.juniemvc.service
- Class: BeerOrderService (package-private)
- Annotations: @Service
- Injection: Constructor injection for repositories
- Transactions:
  - @Transactional on data-modifying methods
  - @Transactional(readOnly = true) on query methods
- Methods (initial boundary):
  - BeerOrder placeOrder(BeerOrder draft)
    - Saving BeerOrder cascades and persists lines due to cascade = ALL
  - List<BeerOrder> getOrdersForCustomer(String customerRef)

## Controller and DTO Guidance
- Do not expose entities in controllers. Define DTOs for requests and responses under org.example.juniemvc.models (e.g., BeerOrderDTO, BeerOrderLineDTO) similar to existing BeerDTO.
- Use MapStruct mappers (org.example.juniemvc.mappers) to convert between entities and DTOs.
- Validation: apply Jakarta Validation on request DTOs (e.g., @NotNull for beerId, @Positive for orderQuantity, etc.).
- REST style:
  - Versioned URLs recommended: /api/v1/beer-orders
  - POST /api/v1/beer-orders -> 201 Created with Location header and response body
  - GET /api/v1/beer-orders?customerRef=... -> 200 OK with collection
- JSON should use an object at top level; maintain camelCase consistently.

## Serialization and Recursion Safety
- Prefer DTOs to avoid cyclic serialization.
- If entities are ever serialized during development, use @ToString(exclude = ...) and consider Jackson’s @JsonIgnore or @JsonBackReference/@JsonManagedReference on one side (temporary safeguard). Production code should not serialize entities.

## Performance and OSIV
- Keep FetchType.LAZY on collections and @ManyToOne.
- Disable OSIV (spring.jpa.open-in-view=false) as per guidelines; fetch needed associations explicitly using EntityGraph or JPQL fetch joins in repository queries.

## Logging
- Use a proper logging framework (SLF4J). No System.out.println. Do not log sensitive data.

## Testing Requirements
- Unit tests
  - Persist a BeerOrder with multiple lines, verify lines are saved with proper FKs (cascading works).
  - Removing a line and saving should delete the line (orphanRemoval = true).
  - LAZY loading works within @Transactional; outside it, use repository methods with fetch strategies.
- Integration tests
  - Start app on random port when testing controller endpoints.
  - Keep OSIV disabled.

## Acceptance Criteria
1. New entities BeerOrder and BeerOrderLine are implemented per the specifications above with correct JPA mappings and Lombok annotations.
2. Relationships match ERD: BeerOrder 1..* BeerOrderLine; BeerOrderLine *..1 Beer; owning side is BeerOrderLine with beer_order_id and beer_id FKs.
3. Repositories for BeerOrder and BeerOrderLine exist and basic queries compile; optional EntityGraph added for eager read use cases.
4. A service class BeerOrderService exists, uses constructor injection, and defines transactional methods for placing orders and retrieving orders by customerRef.
5. Entities avoid recursion: collections are excluded from toString; no equals/hashCode based on collections.
6. Controllers (when added) use DTOs, not entities, and validate input.
7. Application builds successfully and tests for persistence behavior pass.

## Implementation Notes
- Keep Spring components (controllers, services) package-private where possible; entities must remain public.
- Prefer command/DTO objects for use cases in services.
- Consider future enum for order and line statuses.
- Keep update/creation timestamps managed by Hibernate annotations.

## Migration/DDL (Optional but Recommended)
- If using Flyway/Liquibase, add migrations to create beer_order and beer_order_line tables with the FKs and indexes described above.
