package org.example.juniemvc.repositories;

import org.example.juniemvc.entities.BeerOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeerOrderLineRepository extends JpaRepository<BeerOrderLine, Integer> {
}
