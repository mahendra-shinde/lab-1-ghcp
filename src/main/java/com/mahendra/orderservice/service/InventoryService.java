package com.mahendra.orderservice.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InventoryService {

    private final Map<String, Integer> inventoryMap = new HashMap<>();

    public InventoryService() {
        inventoryMap.put("PHONE-01", 10);
    }

    public boolean reserveStock(String sku, int quantity) {
        Integer currentStock = inventoryMap.getOrDefault(sku, 0);
        if (currentStock >= quantity) {
            inventoryMap.put(sku, currentStock - quantity);
            return true;
        }
        return false;
    }

    public int getStock(String sku) {
        return inventoryMap.getOrDefault(sku, 0);
    }
}
