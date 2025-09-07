package org.example.juniemvc.controllers;

import java.net.URI;
import java.util.List;
import org.example.juniemvc.entities.Beer;
import org.example.juniemvc.service.BeerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/beers")
public class BeerController {

    private final BeerService beerService;

    public BeerController(BeerService beerService) {
        this.beerService = beerService;
    }

    @GetMapping
    public List<Beer> listAll() {
        return beerService.findAll();
    }

    @GetMapping("/{id}")
    public Beer getById(@PathVariable Integer id) {
        return beerService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beer not found"));
    }

    @PostMapping
    public ResponseEntity<Beer> create(@RequestBody Beer beer) {
        Beer created = beerService.create(beer);
        return ResponseEntity.created(URI.create("/api/beers/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public Beer update(@PathVariable Integer id, @RequestBody Beer beer) {
        return beerService.update(id, beer)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beer not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        boolean deleted = beerService.deleteById(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Beer not found");
    }
}
