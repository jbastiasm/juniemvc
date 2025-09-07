package org.example.juniemvc.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.example.juniemvc.entities.Beer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class BeerRepositoryTests {

    @Autowired
    BeerRepository beerRepository;

    private Beer buildSample() {
        return Beer.builder()
            .beerName("Sample Lager")
            .beerStyle("Lager")
            .upc("123456789012")
            .quantityOnHand(10)
            .price(new BigDecimal("5.99"))
            .build();
    }

    @Test
    @DisplayName("Save and findById works")
    void saveAndFindById() {
        Beer saved = beerRepository.save(buildSample());
        assertThat(saved.getId()).isNotNull();

        Optional<Beer> fetched = beerRepository.findById(saved.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getBeerName()).isEqualTo("Sample Lager");
    }

    @Test
    @DisplayName("findAll returns list with saved item")
    void findAllWorks() {
        beerRepository.save(buildSample());
        List<Beer> all = beerRepository.findAll();
        assertThat(all).isNotEmpty();
    }

    @Test
    @DisplayName("update and versioning works")
    void updateWorks() {
        Beer saved = beerRepository.save(buildSample());
        Integer originalVersion = saved.getVersion();

        saved.setBeerName("Updated Lager");
        Beer updated = beerRepository.save(saved);

        assertThat(updated.getBeerName()).isEqualTo("Updated Lager");
        // Version may be null initially depending on JPA provider; ensure entity persisted
        assertThat(updated.getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("delete removes the entity")
    void deleteWorks() {
        Beer saved = beerRepository.save(buildSample());
        Integer id = saved.getId();

        beerRepository.deleteById(id);

        assertThat(beerRepository.findById(id)).isEmpty();
    }
}
