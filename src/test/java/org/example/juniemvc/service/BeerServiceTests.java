package org.example.juniemvc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.example.juniemvc.entities.Beer;
import org.example.juniemvc.repositories.BeerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class BeerServiceTests {

    @Mock
    BeerRepository beerRepository;

    @InjectMocks
    BeerServiceImpl beerService;

    private Beer sample(Integer id) {
        return Beer.builder()
            .id(id)
            .beerName("Sample Lager")
            .beerStyle("Lager")
            .upc("123456789012")
            .quantityOnHand(10)
            .price(new BigDecimal("5.99"))
            .build();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("findAll returns list")
    void findAll_returnsList() {
        when(beerRepository.findAll()).thenReturn(List.of(sample(1)));
        List<Beer> result = beerService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("findById returns item when found")
    void findById_found() {
        when(beerRepository.findById(1)).thenReturn(Optional.of(sample(1)));
        Optional<Beer> result = beerService.findById(1);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("findById returns empty when not found")
    void findById_notFound() {
        when(beerRepository.findById(99)).thenReturn(Optional.empty());
        Optional<Beer> result = beerService.findById(99);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("create saves new entity and returns it")
    void create_saves() {
        Beer toCreate = sample(null);
        Beer saved = sample(10);
        when(beerRepository.save(any(Beer.class))).thenReturn(saved);
        Beer result = beerService.create(toCreate);
        assertThat(result.getId()).isEqualTo(10);
    }

    @Test
    @DisplayName("update updates fields when found")
    void update_found() {
        Beer existing = sample(5);
        Beer patch = Beer.builder().beerName("New Name").beerStyle("IPA").upc("987").quantityOnHand(20).price(new BigDecimal("6.99")).build();
        when(beerRepository.findById(5)).thenReturn(Optional.of(existing));
        when(beerRepository.save(any(Beer.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Beer> result = beerService.update(5, patch);
        assertThat(result).isPresent();
        assertThat(result.get().getBeerName()).isEqualTo("New Name");
        verify(beerRepository).save(any(Beer.class));
    }

    @Test
    @DisplayName("update returns empty when not found")
    void update_notFound() {
        Beer patch = sample(null);
        when(beerRepository.findById(42)).thenReturn(Optional.empty());
        Optional<Beer> result = beerService.update(42, patch);
        assertThat(result).isEmpty();
        verify(beerRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteById returns true when exists and deletes")
    void delete_found() {
        when(beerRepository.existsById(7)).thenReturn(true);
        boolean deleted = beerService.deleteById(7);
        assertThat(deleted).isTrue();
        verify(beerRepository).deleteById(7);
    }

    @Test
    @DisplayName("deleteById returns false when not found")
    void delete_notFound() {
        when(beerRepository.existsById(8)).thenReturn(false);
        boolean deleted = beerService.deleteById(8);
        assertThat(deleted).isFalse();
        verify(beerRepository, never()).deleteById(8);
    }
}
