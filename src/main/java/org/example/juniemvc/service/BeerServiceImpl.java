package org.example.juniemvc.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.example.juniemvc.entities.Beer;
import org.example.juniemvc.mappers.BeerMapper;
import org.example.juniemvc.models.BeerDTO;
import org.example.juniemvc.repositories.BeerRepository;
import org.springframework.stereotype.Service;

@Service
public class BeerServiceImpl implements BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    public BeerServiceImpl(BeerRepository beerRepository, BeerMapper beerMapper) {
        this.beerRepository = beerRepository;
        this.beerMapper = beerMapper;
    }

    @Override
    public List<BeerDTO> findAll() {
        return beerRepository.findAll().stream()
            .map(beerMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<BeerDTO> findById(Integer id) {
        return beerRepository.findById(id).map(beerMapper::toDto);
    }

    @Override
    public BeerDTO create(BeerDTO beer) {
        Beer entity = beerMapper.toEntity(beer);
        // Ensure ID is null so JPA treats as new entity
        entity.setId(null);
        Beer saved = beerRepository.save(entity);
        return beerMapper.toDto(saved);
    }

    @Override
    public Optional<BeerDTO> update(Integer id, BeerDTO beer) {
        return beerRepository.findById(id).map(existing -> {
            beerMapper.updateEntityFromDto(beer, existing);
            Beer saved = beerRepository.save(existing);
            return beerMapper.toDto(saved);
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
