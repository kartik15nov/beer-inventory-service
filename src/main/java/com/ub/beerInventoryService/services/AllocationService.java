package com.ub.beerInventoryService.services;

import com.ub.brewery.model.BeerOrderDto;

public interface AllocationService {
    Boolean allocateOrder(BeerOrderDto beerOrderDto);

    void deAllocateOrder(BeerOrderDto beerOrderDto);
}
