package com.mahendra.orderservice.service;

import com.mahendra.orderservice.dto.OrderRequest;
import com.mahendra.orderservice.dto.OrderResponse;

import java.util.Optional;
import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);

    Optional<OrderResponse> getOrder(UUID id);
}
