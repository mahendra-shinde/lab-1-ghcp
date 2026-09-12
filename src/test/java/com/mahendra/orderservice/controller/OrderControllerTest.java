package com.mahendra.orderservice.controller;

import com.mahendra.orderservice.dto.OrderResponse;
import com.mahendra.orderservice.exception.GlobalExceptionHandler;
import com.mahendra.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void getOrderReturnsProblemDetailWhenOrderDoesNotExist() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderService.getOrder(orderId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/errors/not-found"))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Order with id " + orderId + " not found"));
    }

    @Test
    void createOrderReturnsProblemDetailWhenValidationFails() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "",
                                  "items": [
                                    {
                                      "sku": "",
                                      "quantity": 0,
                                      "unitPrice": 0
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/errors/bad-request"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(4));
    }
}
