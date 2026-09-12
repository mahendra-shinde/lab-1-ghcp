package com.mahendra.orderservice.service;

import com.mahendra.orderservice.dto.OrderItemRequest;
import com.mahendra.orderservice.dto.OrderRequest;
import com.mahendra.orderservice.dto.OrderResponse;
import com.mahendra.orderservice.dto.OrderStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal DISCOUNT_MULTIPLIER = new BigDecimal("0.90");
    private static final String LOYALTY_PLATINUM_PREFIX = "LOYALTY-PLATINUM-";
    private static final String LOYALTY_GOLD_PREFIX = "LOYALTY-GOLD-";
    private static final BigDecimal LOYALTY_PLATINUM_RATE = new BigDecimal("0.15");
    private static final BigDecimal LOYALTY_GOLD_RATE = new BigDecimal("0.05");
    private static final String DISCONTINUED_PREFIX = "DISCONTINUED-";
    private static final String DISCONTINUED_REASON = "Order contains discontinued items";

    private final ConcurrentMap<UUID, OrderResponse> orders = new ConcurrentHashMap<>();

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        UUID orderId = UUID.randomUUID();
        List<OrderItemRequest> items = List.copyOf(request.items());

        BigDecimal rawTotal = items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean hasDiscontinuedItems = items.stream()
                .map(OrderItemRequest::sku)
                .anyMatch(sku -> sku.startsWith(DISCONTINUED_PREFIX));

        BigDecimal discountedTotal = rawTotal;
        if (!hasDiscontinuedItems) {
            BigDecimal subtotalAfterVolume = rawTotal.compareTo(DISCOUNT_THRESHOLD) > 0
                    ? rawTotal.multiply(DISCOUNT_MULTIPLIER)
                    : rawTotal;
            BigDecimal loyaltyRate = resolveLoyaltyRate(request.customerId());
            discountedTotal = subtotalAfterVolume
                    .multiply(BigDecimal.ONE.subtract(loyaltyRate))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        OrderStatus status = hasDiscontinuedItems ? OrderStatus.REJECTED : OrderStatus.APPROVED;
        String rejectionReason = hasDiscontinuedItems ? DISCONTINUED_REASON : null;

        OrderResponse response = new OrderResponse(
                orderId,
                request.customerId(),
                items,
                rawTotal,
                discountedTotal,
                status,
                rejectionReason
        );

        orders.compute(orderId, (key, existing) -> response);
        return response;
    }

    @Override
    public Optional<OrderResponse> getOrder(UUID id) {
        return Optional.ofNullable(orders.get(id));
    }

    private BigDecimal resolveLoyaltyRate(String customerId) {
        if (customerId.startsWith(LOYALTY_PLATINUM_PREFIX)) {
            return LOYALTY_PLATINUM_RATE;
        }
        if (customerId.startsWith(LOYALTY_GOLD_PREFIX)) {
            return LOYALTY_GOLD_RATE;
        }
        return BigDecimal.ZERO;
    }
}
