package com.mahendra.orderservice;

import com.mahendra.orderservice.dto.OrderItemRequest;
import com.mahendra.orderservice.dto.OrderRequest;
import com.mahendra.orderservice.dto.OrderResponse;
import com.mahendra.orderservice.dto.OrderStatus;
import com.mahendra.orderservice.service.OrderService;
import com.mahendra.orderservice.service.OrderServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceTest {

    private final OrderService orderService = new OrderServiceImpl();

    @Test
    void createOrderCalculatesRawAndDiscountedTotals() {
        OrderRequest request = new OrderRequest(
                "customer-1",
                List.of(
                        new OrderItemRequest("SKU-1", 5, new BigDecimal("80.00")),
                        new OrderItemRequest("SKU-2", 2, new BigDecimal("70.00"))
                )
        );

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.rawTotal()).isEqualByComparingTo("540.00");
        assertThat(response.discountedTotal()).isEqualByComparingTo("486.00");
        assertThat(response.status()).isEqualTo(OrderStatus.APPROVED);
        assertThat(response.rejectionReason()).isNull();
    }

    @Test
    void createOrderRejectsDiscontinuedItems() {
        OrderRequest request = new OrderRequest(
                "customer-2",
                List.of(new OrderItemRequest("DISCONTINUED-ABC", 1, new BigDecimal("10.00")))
        );

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.status()).isEqualTo(OrderStatus.REJECTED);
        assertThat(response.rejectionReason()).isEqualTo("Order contains discontinued items");
    }

    @Test
    void getOrderReturnsStoredOrderWhenPresent() {
        OrderRequest request = new OrderRequest(
                "customer-3",
                List.of(new OrderItemRequest("SKU-3", 1, new BigDecimal("9.99")))
        );

        OrderResponse created = orderService.createOrder(request);

        assertThat(orderService.getOrder(created.id()))
                .contains(created);
    }

    @Test
    void getOrderReturnsEmptyWhenOrderMissing() {
        assertThat(orderService.getOrder(UUID.randomUUID())).isEmpty();
    }
}
