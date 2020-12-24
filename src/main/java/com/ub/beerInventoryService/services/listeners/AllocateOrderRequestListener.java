package com.ub.beerInventoryService.services.listeners;

import com.ub.beerInventoryService.config.JMSConfig;
import com.ub.beerInventoryService.services.AllocationService;
import com.ub.brewery.events.AllocateOrderRequest;
import com.ub.brewery.events.AllocateOrderResult;
import com.ub.brewery.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AllocateOrderRequestListener {

    private final JmsTemplate jmsTemplate;
    private final AllocationService allocationService;

    @JmsListener(destination = JMSConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(AllocateOrderRequest request) {
        BeerOrderDto beerOrderDto = request.getBeerOrderDto();

        boolean pendingInventory = false;
        boolean allocationError = false;

        try {
            pendingInventory = allocationService.allocateOrder(beerOrderDto);
        } catch (Exception e) {
            log.error("Allocation failed for Order Id: {}", beerOrderDto.getId());
            allocationError = true;
        }

        AllocateOrderResult allocateOrderResult = AllocateOrderResult.builder()
                .beerOrderDto(beerOrderDto)
                .pendingInventory(pendingInventory)
                .allocationError(allocationError).build();

        jmsTemplate.convertAndSend(JMSConfig.ALLOCATE_ORDER_RESPONSE_QUEUE, allocateOrderResult);
    }
}
