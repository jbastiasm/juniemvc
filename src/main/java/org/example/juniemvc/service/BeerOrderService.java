package org.example.juniemvc.service;

import org.example.juniemvc.entities.Beer;
import org.example.juniemvc.entities.BeerOrder;
import org.example.juniemvc.entities.BeerOrderLine;
import org.example.juniemvc.repositories.BeerOrderRepository;
import org.example.juniemvc.repositories.BeerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
class BeerOrderService {

    private static final Logger log = LoggerFactory.getLogger(BeerOrderService.class);

    private final BeerOrderRepository beerOrderRepository;
    private final BeerRepository beerRepository;

    BeerOrderService(BeerOrderRepository beerOrderRepository, BeerRepository beerRepository) {
        this.beerOrderRepository = beerOrderRepository;
        this.beerRepository = beerRepository;
    }

    @Transactional
    BeerOrder placeOrder(BeerOrder draft) {
        Objects.requireNonNull(draft, "BeerOrder draft must not be null");
        if (draft.getLines() == null || draft.getLines().isEmpty()) {
            throw new IllegalArgumentException("BeerOrder must contain at least one line");
        }
        // Validate referenced beers exist
        for (BeerOrderLine line : draft.getLines()) {
            if (line.getBeer() == null || line.getBeer().getId() == null) {
                throw new IllegalArgumentException("Each order line must reference a beer id");
            }
            Integer beerId = line.getBeer().getId();
            Beer beer = beerRepository.findById(beerId)
                    .orElseThrow(() -> new IllegalArgumentException("Beer not found: id=" + beerId));
            // attach managed reference (id-only is fine, but ensure consistency)
            line.setBeer(beer);
            // maintain back-reference
            line.setBeerOrder(draft);
        }
        BeerOrder saved = beerOrderRepository.save(draft);
        log.info("Placed BeerOrder id={} with {} line(s)", saved.getId(), saved.getLines() != null ? saved.getLines().size() : 0);
        return saved;
    }

    @Transactional(readOnly = true)
    List<BeerOrder> getOrdersForCustomer(String customerRef) {
        return beerOrderRepository.findByCustomerRef(customerRef);
    }
}
