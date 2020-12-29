package com.ub.beerInventoryService.services.listeners;

import com.ub.beerInventoryService.config.JMSConfig;
import com.ub.beerInventoryService.services.AllocationService;
import com.ub.brewery.events.DeAllocateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DeAllocationListener {

    private final AllocationService allocationService;

    @JmsListener(destination = JMSConfig.DEALLOCATE_ORDER_QUEUE)
    public void listen(DeAllocateOrderRequest request) {
        allocationService.deAllocateOrder(request.getBeerOrderDto());
    }
}
