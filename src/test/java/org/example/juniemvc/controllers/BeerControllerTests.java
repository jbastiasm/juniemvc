package org.example.juniemvc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.example.juniemvc.entities.Beer;
import org.example.juniemvc.service.BeerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BeerController.class)
class BeerControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BeerService beerService;

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

    @Test
    @DisplayName("GET /api/beers returns list")
    void listAll_returnsOkAndList() throws Exception {
        given(beerService.findAll()).willReturn(List.of(sample(1)));

        mockMvc.perform(get("/api/beers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].beerName").value("Sample Lager"));
    }

    @Test
    @DisplayName("GET /api/beers/{id} returns item when found")
    void getById_found_returnsOk() throws Exception {
        given(beerService.findById(eq(1))).willReturn(Optional.of(sample(1)));

        mockMvc.perform(get("/api/beers/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.beerName").value("Sample Lager"));
    }

    @Test
    @DisplayName("GET /api/beers/{id} returns 404 when not found")
    void getById_notFound_returns404() throws Exception {
        given(beerService.findById(eq(99))).willReturn(Optional.empty());

        mockMvc.perform(get("/api/beers/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/beers creates and returns 201 with Location")
    void create_returns201WithLocationAndBody() throws Exception {
        Beer toCreate = sample(null);
        Beer created = sample(10);
        given(beerService.create(any(Beer.class))).willReturn(created);

        String json = objectMapper.writeValueAsString(toCreate);

        mockMvc.perform(post("/api/beers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/beers/10"))
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.beerName").value("Sample Lager"));
    }

    @Test
    @DisplayName("PUT /api/beers/{id} updates and returns 200")
    void update_returns200AndBody() throws Exception {
        Beer update = sample(null);
        Beer updated = sample(2);
        given(beerService.update(eq(2), any(Beer.class))).willReturn(Optional.of(updated));
        String json = objectMapper.writeValueAsString(update);

        mockMvc.perform(put("/api/beers/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.beerName").value("Sample Lager"));
    }

    @Test
    @DisplayName("PUT /api/beers/{id} returns 404 when not found")
    void update_notFound_returns404() throws Exception {
        Beer update = sample(null);
        given(beerService.update(eq(99), any(Beer.class))).willReturn(Optional.empty());
        String json = objectMapper.writeValueAsString(update);

        mockMvc.perform(put("/api/beers/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/beers/{id} returns 204 when deleted")
    void delete_returns204() throws Exception {
        given(beerService.deleteById(eq(3))).willReturn(true);
        mockMvc.perform(delete("/api/beers/3"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/beers/{id} returns 404 when not found")
    void delete_notFound_returns404() throws Exception {
        given(beerService.deleteById(eq(77))).willReturn(false);
        mockMvc.perform(delete("/api/beers/77"))
            .andExpect(status().isNotFound());
    }
}
