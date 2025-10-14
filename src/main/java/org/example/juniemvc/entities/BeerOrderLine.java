package org.example.juniemvc.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a Beer Order Line.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "beer_order_line", indexes = {
        @Index(name = "idx_beer_order_line_order", columnList = "beer_order_id"),
        @Index(name = "idx_beer_order_line_beer", columnList = "beer_id")
})
public class BeerOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Integer version;

    private Integer orderQuantity;

    private Integer quantityAllocated;

    private String status;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updateDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beer_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_beer_order_line_order"))
    private BeerOrder beerOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_beer_order_line_beer"))
    private Beer beer;
}
