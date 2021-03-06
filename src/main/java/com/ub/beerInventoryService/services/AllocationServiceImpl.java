package com.ub.beerInventoryService.services;

import com.ub.beerInventoryService.domain.BeerInventory;
import com.ub.beerInventoryService.repositories.BeerInventoryRepository;
import com.ub.brewery.model.BeerOrderDto;
import com.ub.brewery.model.BeerOrderLineDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class AllocationServiceImpl implements AllocationService {

    private final BeerInventoryRepository beerInventoryRepository;

    @Override
    public Boolean allocateOrder(BeerOrderDto beerOrderDto) {
        log.debug("Allocating OrderId: {}", beerOrderDto.getId());

        AtomicInteger totalOrdered = new AtomicInteger();
        AtomicInteger totalAllocated = new AtomicInteger();

        beerOrderDto.getBeerOrderLines()
                .forEach(beerOrderLineDto -> {
                    int orderedQty = beerOrderLineDto.getOrderQuantity() != null ? beerOrderLineDto.getOrderQuantity() : 0;
                    int allocatedQty = beerOrderLineDto.getQuantityAllocated() != null ? beerOrderLineDto.getQuantityAllocated() : 0;

                    if (orderedQty - allocatedQty > 0)
                        allocateBeerOrderLine(beerOrderLineDto);

                    totalAllocated.set(totalOrdered.get() + (beerOrderLineDto.getOrderQuantity() != null ? beerOrderLineDto.getOrderQuantity() : 0));
                    totalAllocated.set(totalAllocated.get() + (beerOrderLineDto.getQuantityAllocated() != null ? beerOrderLineDto.getQuantityAllocated() : 0));
                });

        log.debug("Total Ordered: {}, Total Allocated: {}", totalOrdered.get(), totalAllocated.get());

        return totalOrdered.get() == totalAllocated.get();
    }

    @Override
    public void deAllocateOrder(BeerOrderDto beerOrderDto) {
        beerOrderDto.getBeerOrderLines()
                .forEach(beerOrderLineDto -> {
                    BeerInventory beerInventory = BeerInventory.builder()
                            .beerId(beerOrderLineDto.getBeerId())
                            .upc(beerOrderLineDto.getUpc())
                            .quantityOnHand(beerOrderLineDto.getQuantityAllocated())
                            .build();

                    BeerInventory savedInventory = beerInventoryRepository.save(beerInventory);

                    log.debug("Saved Inventory for Beer UPC: {}, Inventory id: {}", savedInventory.getUpc(), savedInventory.getId());
                });
    }

    private void allocateBeerOrderLine(BeerOrderLineDto beerOrderLineDto) {
        List<BeerInventory> inventoryList = beerInventoryRepository.findAllByUpc(beerOrderLineDto.getUpc());

        inventoryList.forEach(beerInventory -> {
            int qtyOnHand = beerInventory.getQuantityOnHand() != null ? beerInventory.getQuantityOnHand() : 0;
            int orderQty = beerOrderLineDto.getOrderQuantity() != null ? beerOrderLineDto.getOrderQuantity() : 0;
            int allocatedQty = beerOrderLineDto.getQuantityAllocated() != null ? beerOrderLineDto.getQuantityAllocated() : 0;

            int qtyToAllocate = orderQty - allocatedQty;

            if (qtyOnHand >= qtyToAllocate) { //Full Allocation
                qtyOnHand -= qtyToAllocate;

                beerOrderLineDto.setQuantityAllocated(orderQty);

                beerInventory.setQuantityOnHand(qtyOnHand);
                beerInventoryRepository.save(beerInventory);
            } else if (qtyOnHand > 0) { //Partial Allocation
                beerOrderLineDto.setQuantityAllocated(allocatedQty + qtyOnHand);
                beerInventory.setQuantityOnHand(0);
            }

            if (beerInventory.getQuantityOnHand() == 0)
                beerInventoryRepository.delete(beerInventory);
        });
    }
}
