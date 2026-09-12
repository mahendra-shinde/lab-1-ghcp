package com.mahendra.orderservice;

import com.mahendra.orderservice.dto.OrderItemRequest;
import com.mahendra.orderservice.dto.OrderRequest;
import com.mahendra.orderservice.dto.OrderResponse;
import com.mahendra.orderservice.dto.OrderStatus;
import com.mahendra.orderservice.service.OrderService;
import com.mahendra.orderservice.service.OrderServiceImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceTest {

    private final OrderService orderService = new OrderServiceImpl();

    @ParameterizedTest
    @MethodSource("discountPermutations")
    void createOrderCalculatesDiscountPermutations(String customerId, List<OrderItemRequest> items, String expectedRawTotal, String expectedDiscountedTotal) {
        OrderRequest request = new OrderRequest(
                customerId,
                items
        );

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.rawTotal()).isEqualByComparingTo(expectedRawTotal);
        assertThat(response.discountedTotal()).isEqualByComparingTo(expectedDiscountedTotal);
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
        assertThat(response.discountedTotal()).isEqualByComparingTo(response.rawTotal());
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

    private static Stream<Arguments> discountPermutations() {
        return Stream.of(
                Arguments.of(
                        "LOYALTY-GOLD-1",
                        List.of(
                                new OrderItemRequest("SKU-1", 5, new BigDecimal("80.00")),
                                new OrderItemRequest("SKU-2", 2, new BigDecimal("70.00"))
                        ),
                        "540.00",
                        "461.70"
                ),
                Arguments.of(
                        "LOYALTY-PLATINUM-1",
                        List.of(
                                new OrderItemRequest("SKU-3", 2, new BigDecimal("100.00"))
                        ),
                        "200.00",
                        "170.00"
                ),
                Arguments.of(
                        "customer-1",
                        List.of(
                                new OrderItemRequest("SKU-4", 6, new BigDecimal("90.00"))
                        ),
                        "540.00",
                        "486.00"
                )
        );
    }
}
