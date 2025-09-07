package org.example.juniemvc.service;

import java.util.List;
import java.util.Optional;
import org.example.juniemvc.entities.Beer;

public interface BeerService {

    List<Beer> findAll();

    Optional<Beer> findById(Integer id);

    Beer create(Beer beer);

    Optional<Beer> update(Integer id, Beer beer);

    boolean deleteById(Integer id);
}
