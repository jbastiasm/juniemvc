Analyze the attached ERD (BeerOrder —< BeerOrderLine >— Beer) and follow these detailed, step-by-step instructions to model the relationships in Spring Data JPA using Lombok.

Scope
- Define two new entities: BeerOrder and BeerOrderLine.
- Reuse the existing Beer entity in this project.
- Map relationships and lifecycle rules exactly as indicated by the ERD.
- Provide repositories, basic service boundaries, and notes to avoid common pitfalls (equals/hashCode, toString recursion, fetch types, cascading, etc.).

Conventions and Guidelines
- Prefer constructor injection for services and components.
- Use package‑private visibility for Spring components and configuration where possible.
- Keep domain entities public (JPA requirement) but keep controller/service classes package‑private.
- Disable OSIV in application.properties (already recommended in guidelines), and fetch what you need explicitly.
- Use DTOs in controllers; do not expose entities directly.

1) Entity: Beer (already present)
   Fields from ERD:
- id: Integer (PK)
- version: Integer (@Version)
- beerName: String
- beerStyle: String
- upc: String
- quantityOnHand: Integer
- price: BigDecimal
- createdDate: LocalDateTime (@CreationTimestamp)
- updateDate: LocalDateTime (@UpdateTimestamp)
  Note: Exists at src/main/java/.../entities/Beer.java. No relationship fields are required here for the order feature (keep the association owned by BeerOrderLine to avoid bidirectional clutter from Beer -> lines).

2) Entity: BeerOrder
   Class: org.example.juniemvc.entities.BeerOrder
   Annotations and Lombok:
- @Entity, @Table(name = "beer_order")
- @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
- Consider @EqualsAndHashCode(onlyExplicitlyIncluded = true) to avoid using collections.

Fields:
- @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
- @Version private Integer version;
- private String customerRef;
- private BigDecimal paymentAmount;
- private String status; // Consider an enum later (e.g., NEW, PAID, SHIPPED)
- @CreationTimestamp private LocalDateTime createdDate;
- @UpdateTimestamp private LocalDateTime updateDate;

Relationships:
- OneToMany to BeerOrderLine (bidirectional), mappedBy = "beerOrder", cascade = CascadeType.ALL, orphanRemoval = true.
  private List<BeerOrderLine> lines = new ArrayList<>();

Utility methods to keep both sides in sync:
- addLine(BeerOrderLine line): sets line.setBeerOrder(this) and adds to lines.
- removeLine(BeerOrderLine line): sets line.setBeerOrder(null) and removes from lines.

FetchType:
- For OneToMany, default is LAZY (recommended). Do not force EAGER.

3) Entity: BeerOrderLine
   Class: org.example.juniemvc.entities.BeerOrderLine
   Annotations and Lombok:
- @Entity, @Table(name = "beer_order_line")
- @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
- Consider @EqualsAndHashCode(onlyExplicitlyIncluded = true)

Fields:
- @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
- @Version private Integer version;
- private Integer orderQuantity;
- private Integer quantityAllocated;
- private String status;
- @CreationTimestamp private LocalDateTime createdDate;
- @UpdateTimestamp private LocalDateTime updateDate;

Relationships:
- @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "beer_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_beer_order_line_order"))
  private BeerOrder beerOrder;

- @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "beer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_beer_order_line_beer"))
  private Beer beer;

Notes:
- The BeerOrderLine is the owning side of the BeerOrder <-> BeerOrderLine association (it holds the beer_order_id FK).
- Keep both @ManyToOne as LAZY to avoid N+1 in serialization; never expose entities directly via controllers.

4) Avoiding recursion and performance issues
- Do NOT include the collection field lines in BeerOrder.equals()/hashCode() or toString().
- Optionally add Lombok: @ToString(exclude = "lines") on BeerOrder and @ToString.Exclude on back-references to avoid infinite recursion.
- When mapping to DTOs, control which direction you traverse to prevent loading large graphs accidentally.

5) DDL and table naming
- Tables: beer (existing), beer_order, beer_order_line.
- Columns for FKs in beer_order_line: beer_order_id, beer_id.
- Add indexes using @Table(indexes = @Index(name = "idx_bol_order", columnList = "beer_order_id"), @Index(name = "idx_bol_beer", columnList = "beer_id")) if needed for queries.

6) Example code snippets

BeerOrder.java
--------------------------------------------------
package org.example.juniemvc.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "lines")
@Entity
@Table(name = "beer_order")
public class BeerOrder {
@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id;

    @Version
    private Integer version;

    private String customerRef;
    private BigDecimal paymentAmount;
    private String status;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updateDate;

    @Builder.Default
    @OneToMany(mappedBy = "beerOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BeerOrderLine> lines = new ArrayList<>();

    public void addLine(BeerOrderLine line) {
        line.setBeerOrder(this);
        this.lines.add(line);
    }

    public void removeLine(BeerOrderLine line) {
        line.setBeerOrder(null);
        this.lines.remove(line);
    }
}

BeerOrderLine.java
--------------------------------------------------
package org.example.juniemvc.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "beer_order_line")
public class BeerOrderLine {
@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id;

    @Version
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beer_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_beer_order_line_order"))
    @ToString.Exclude
    private BeerOrder beerOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_beer_order_line_beer"))
    @ToString.Exclude
    private Beer beer;

    private Integer orderQuantity;
    private Integer quantityAllocated;
    private String status;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updateDate;
}

7) Repositories
--------------------------------------------------
package org.example.juniemvc.repositories;

import org.example.juniemvc.entities.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

interface BeerOrderRepository extends JpaRepository<BeerOrder, Integer> {
@EntityGraph(attributePaths = {"lines", "lines.beer"})
List<BeerOrder> findByCustomerRef(String customerRef);
}

interface BeerOrderLineRepository extends JpaRepository<BeerOrderLine, Integer> { }

// BeerRepository already exists.

8) Service layer (boundary and transactions)
--------------------------------------------------
package org.example.juniemvc.service;

import lombok.RequiredArgsConstructor;
import org.example.juniemvc.entities.*;
import org.example.juniemvc.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class BeerOrderService {
private final BeerOrderRepository orderRepo;
private final BeerOrderLineRepository lineRepo;
private final BeerRepository beerRepo;

    @Transactional
    public BeerOrder placeOrder(BeerOrder draft) {
        // Since cascade = ALL, saving the order will persist lines as well
        return orderRepo.save(draft);
    }

    @Transactional(readOnly = true)
    public List<BeerOrder> getOrdersForCustomer(String customerRef) {
        return orderRepo.findByCustomerRef(customerRef);
    }
}

9) DTOs and mapping (controller safety)
- Define request/response DTOs for BeerOrder and BeerOrderLine to prevent exposing entities.
- MapStruct is recommended (project already uses it for Beer).
- Annotate request DTOs with Jakarta Validation annotations (e.g., @NotNull for beerId and orderQuantity).

10) JSON serialization tips
- Do not serialize entities directly. If you accidentally do, bidirectional relationships can cause infinite recursion. Prefer DTOs. If you must serialize entities temporarily, use @JsonManagedReference/@JsonBackReference or @JsonIgnore on one side in addition to Lombok’s ToString exclusions.

11) Testing notes
- Unit test mapping and persistence behavior:
    - Saving BeerOrder with 2 lines cascades and creates rows in beer_order_line with proper FKs.
    - Removing a line from the order and saving should delete the line due to orphanRemoval = true.
    - Lazy loading works inside @Transactional; outside, use fetch joins or EntityGraph.
- For integration tests, start app with RANDOM_PORT and disable OSIV.

12) Migration/DDL
- If using Flyway/Liquibase, create migrations for beer_order and beer_order_line reflecting the above schema and FKs.

This document provides all details needed to implement the ERD relationships with JPA and Lombok in this repository.