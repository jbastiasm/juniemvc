package org.example.juniemvc.service;

import java.util.List;
import java.util.Optional;
import org.example.juniemvc.entities.Beer;
import org.example.juniemvc.repositories.BeerRepository;
import org.springframework.stereotype.Service;

@Service
public class BeerServiceImpl implements BeerService {

    private final BeerRepository beerRepository;

    public BeerServiceImpl(BeerRepository beerRepository) {
        this.beerRepository = beerRepository;
    }

    @Override
    public List<Beer> findAll() {
        return beerRepository.findAll();
    }

    @Override
    public Optional<Beer> findById(Integer id) {
        return beerRepository.findById(id);
    }

    @Override
    public Beer create(Beer beer) {
        // Ensure ID is null so JPA treats as new entity
        beer.setId(null);
        return beerRepository.save(beer);
    }

    @Override
    public Optional<Beer> update(Integer id, Beer beer) {
        return beerRepository.findById(id).map(existing -> {
            existing.setBeerName(beer.getBeerName());
            existing.setBeerStyle(beer.getBeerStyle());
            existing.setUpc(beer.getUpc());
            existing.setQuantityOnHand(beer.getQuantityOnHand());
            existing.setPrice(beer.getPrice());
            return beerRepository.save(existing);
        });
    }

    @Override
    public boolean deleteById(Integer id) {
        if (beerRepository.existsById(id)) {
            beerRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
