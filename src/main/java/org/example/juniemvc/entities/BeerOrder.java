package org.example.juniemvc.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity representing a Beer Order.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "lines")
@Entity
@Table(name = "beer_order")
public class BeerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    // Helper methods to maintain both sides
    public void addLine(BeerOrderLine line) {
        if (line == null) return;
        line.setBeerOrder(this);
        this.lines.add(line);
    }

    public void removeLine(BeerOrderLine line) {
        if (line == null) return;
        line.setBeerOrder(null);
        this.lines.remove(line);
    }
}
