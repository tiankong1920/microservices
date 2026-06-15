package com.inventory.inventoryservice.exception;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(Long id) {
        super("Inventory not found with id: " + id);
    }

    public InventoryNotFoundException(String message) {
        super(message);
    }

    public InventoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
