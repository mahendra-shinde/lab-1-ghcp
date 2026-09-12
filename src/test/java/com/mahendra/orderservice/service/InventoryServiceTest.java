package com.mahendra.orderservice.service;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryServiceTest {

    @Test
    void reserveStockSucceedsWhenStockIsAvailable() {
        InventoryService inventoryService = new InventoryService();

        boolean reserved = inventoryService.reserveStock("PHONE-01", 3);

        assertThat(reserved).isTrue();
        assertThat(inventoryService.getStock("PHONE-01")).isEqualTo(7);
    }

    @Test
    void reserveStockFailsWhenStockIsInsufficient() {
        InventoryService inventoryService = new InventoryService();

        boolean reserved = inventoryService.reserveStock("PHONE-01", 11);

        assertThat(reserved).isFalse();
        assertThat(inventoryService.getStock("PHONE-01")).isEqualTo(10);
    }

    @Test
    void testConcurrentAllocation() throws Exception {
        InventoryService inventoryService = new InventoryService();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < 20; i++) {
                futures.add(executorService.submit(() -> {
                    startLatch.await();
                    return inventoryService.reserveStock("PHONE-01", 1);
                }));
            }

            startLatch.countDown();

            int successCount = 0;
            int failureCount = 0;

            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    successCount++;
                } else {
                    failureCount++;
                }
            }

            assertThat(successCount).isEqualTo(10);
            assertThat(failureCount).isEqualTo(10);
            assertThat(inventoryService.getStock("PHONE-01")).isEqualTo(0);
        } finally {
            executorService.shutdownNow();
        }
    }
}
