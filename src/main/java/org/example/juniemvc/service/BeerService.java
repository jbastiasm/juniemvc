package org.example.juniemvc.service;

import java.util.List;
import java.util.Optional;
import org.example.juniemvc.models.BeerDTO;

public interface BeerService {

    List<BeerDTO> findAll();

    Optional<BeerDTO> findById(Integer id);

    BeerDTO create(BeerDTO beer);

    Optional<BeerDTO> update(Integer id, BeerDTO beer);

    boolean deleteById(Integer id);
}
