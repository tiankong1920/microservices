package com.inventory.inventoryservice.controller;

import com.inventory.inventoryservice.dto.InventoryAlertDTO;
import com.inventory.inventoryservice.dto.InventoryAlertThresholdDTO;
import com.inventory.inventoryservice.entity.InventoryAlertLog;
import com.inventory.inventoryservice.service.InventoryAlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class InventoryAlertControllerTest {

    @Mock
    private InventoryAlertService alertService;

    @InjectMocks
    private InventoryAlertController alertController;

    private InventoryAlertThresholdDTO testThreshold;
    private InventoryAlertDTO testAlert;

    @BeforeEach
    void setUp() {
        testThreshold = new InventoryAlertThresholdDTO();
        testThreshold.setId(1L);
        testThreshold.setProductId(100L);
        testThreshold.setLowStockThreshold(10);
        testThreshold.setCriticalStockThreshold(1000);

        testAlert = new InventoryAlertDTO();
        testAlert.setId(1L);
        testAlert.setProductId(100L);
        testAlert.setAlertType("LOW_STOCK");
    }

    @Test
    void testCreateOrUpdateThreshold() {
        when(alertService.createOrUpdateThreshold(any(InventoryAlertThresholdDTO.class)))
                .thenReturn(testThreshold);

        ResponseEntity<InventoryAlertThresholdDTO> response =
                alertController.createOrUpdateThreshold(testThreshold);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().getProductId());
    }

    @Test
    void testGetAllThresholds() {
        when(alertService.getAllThresholds()).thenReturn(List.of(testThreshold));

        ResponseEntity<List<InventoryAlertThresholdDTO>> response = alertController.getAllThresholds();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(10, response.getBody().get(0).getLowStockThreshold());
    }

    @Test
    void testCheckAndAlertWithAlert() {
        when(alertService.checkAndAlert(1L)).thenReturn(testAlert);

        ResponseEntity<InventoryAlertDTO> response = alertController.checkAndAlert(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("LOW_STOCK", response.getBody().getAlertType());
    }

    @Test
    void testCheckAndAlertNoAlert() {
        when(alertService.checkAndAlert(1L)).thenReturn(null);

        ResponseEntity<InventoryAlertDTO> response = alertController.checkAndAlert(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testCheckAllAlerts() {
        when(alertService.checkAndAlertAll()).thenReturn(List.of(testAlert));

        ResponseEntity<List<InventoryAlertDTO>> response = alertController.checkAllAlerts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetUnacknowledgedAlerts() {
        InventoryAlertLog log = new InventoryAlertLog();
        log.setId(1L);
        log.setAcknowledged(false);
        when(alertService.getUnacknowledgedAlerts()).thenReturn(List.of(log));

        ResponseEntity<List<InventoryAlertLog>> response = alertController.getUnacknowledgedAlerts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testAcknowledgeAlert() {
        doNothing().when(alertService).acknowledgeAlert(eq(1L), eq("admin"));

        ResponseEntity<Void> response = alertController.acknowledgeAlert(1L, "admin");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(alertService).acknowledgeAlert(1L, "admin");
    }

    @Test
    void testAcknowledgeAlertWithDefaultUser() {
        doNothing().when(alertService).acknowledgeAlert(eq(1L), eq("system"));

        ResponseEntity<Void> response = alertController.acknowledgeAlert(1L, "system");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(alertService).acknowledgeAlert(1L, "system");
    }
}
