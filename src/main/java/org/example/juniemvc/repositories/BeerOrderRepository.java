package org.example.juniemvc.repositories;

import java.util.List;
import org.example.juniemvc.entities.BeerOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeerOrderRepository extends JpaRepository<BeerOrder, Integer> {

    List<BeerOrder> findByCustomerRef(String customerRef);

    @Override
    @EntityGraph(attributePaths = {"lines", "lines.beer"})
    List<BeerOrder> findAll();
}
