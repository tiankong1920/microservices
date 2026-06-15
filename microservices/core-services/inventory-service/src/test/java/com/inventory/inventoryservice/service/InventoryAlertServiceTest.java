package com.inventory.inventoryservice.service;

import com.inventory.inventoryservice.dto.InventoryAlertDTO;
import com.inventory.inventoryservice.dto.InventoryAlertThresholdDTO;
import com.inventory.inventoryservice.entity.Inventory;
import com.inventory.inventoryservice.entity.InventoryAlertLog;
import com.inventory.inventoryservice.entity.InventoryAlertThreshold;
import com.inventory.inventoryservice.repository.IInventoryAlertLogRepository;
import com.inventory.inventoryservice.repository.IInventoryAlertThresholdRepository;
import com.inventory.inventoryservice.repository.IInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class InventoryAlertServiceTest {

    @Mock
    private IInventoryRepository inventoryRepository;

    @Mock
    private IInventoryAlertThresholdRepository thresholdRepository;

    @Mock
    private IInventoryAlertLogRepository alertLogRepository;

    @Mock
    private InventoryAlertEmailService emailService;

    private InventoryAlertService alertService;

    private Inventory lowStockInventory;
    private Inventory criticalStockInventory;
    private Inventory outOfStockInventory;
    private Inventory normalInventory;
    private InventoryAlertThreshold threshold;

    @BeforeEach
    void setUp() {
        alertService = new InventoryAlertService(inventoryRepository, thresholdRepository, alertLogRepository, emailService);
        lowStockInventory = createInventory(1L, 100L, 1L, 15);
        criticalStockInventory = createInventory(2L, 100L, 1L, 3);
        outOfStockInventory = createInventory(3L, 100L, 1L, 0);
        normalInventory = createInventory(4L, 100L, 1L, 100);

        threshold = createThreshold(100L, 1L, 20, 5, 10, "admin@example.com", true);
    }

    private Inventory createInventory(Long id, Long productId, Long warehouseId, Integer quantity) {
        Inventory inventory = new Inventory();
        inventory.setId(id);
        inventory.setProductId(productId);
        inventory.setWarehouseId(warehouseId);
        inventory.setQuantity(quantity);
        inventory.setAvailableQuantity(quantity);
        return inventory;
    }

    private InventoryAlertThreshold createThreshold(Long productId, Long warehouseId,
                                                     Integer lowStock, Integer criticalStock,
                                                     Integer reorderPoint, String email, boolean enabled) {
        InventoryAlertThreshold t = new InventoryAlertThreshold();
        t.setId(1L);
        t.setProductId(productId);
        t.setWarehouseId(warehouseId);
        t.setLowStockThreshold(lowStock);
        t.setCriticalStockThreshold(criticalStock);
        t.setReorderPoint(reorderPoint);
        t.setAlertEmail(email);
        t.setEnabled(enabled);
        return t;
    }

    @Nested
    @DisplayName("Check and Alert Tests")
    class CheckAndAlertTests {

        @Test
        @DisplayName("Low stock inventory triggers LOW_STOCK alert")
        void lowStockTriggersAlert() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(lowStockInventory));
            when(thresholdRepository.findByProductIdAndWarehouseId(100L, 1L))
                    .thenReturn(Optional.of(threshold));
            when(alertLogRepository.save(any(InventoryAlertLog.class)))
                    .thenAnswer(i -> {
                        InventoryAlertLog log = i.getArgument(0);
                        log.setId(1L);
                        return log;
                    });

            InventoryAlertDTO result = alertService.checkAndAlert(1L);

            assertNotNull(result);
            assertEquals("LOW_STOCK", result.getAlertType());
            assertEquals(15, result.getCurrentQuantity());
            assertEquals(20, result.getThresholdValue());
            verify(emailService).sendLowStockAlert(anyString(), anyString(), anyLong(), anyLong(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("Critical stock inventory triggers CRITICAL_STOCK alert")
        void criticalStockTriggersAlert() {
            when(inventoryRepository.findById(2L)).thenReturn(Optional.of(criticalStockInventory));
            when(thresholdRepository.findByProductIdAndWarehouseId(100L, 1L))
                    .thenReturn(Optional.of(threshold));
            when(alertLogRepository.save(any(InventoryAlertLog.class)))
                    .thenAnswer(i -> {
                        InventoryAlertLog log = i.getArgument(0);
                        log.setId(2L);
                        return log;
                    });

            InventoryAlertDTO result = alertService.checkAndAlert(2L);

            assertNotNull(result);
            assertEquals("CRITICAL_STOCK", result.getAlertType());
            assertEquals(3, result.getCurrentQuantity());
            assertEquals(5, result.getThresholdValue());
            verify(emailService).sendCriticalStockAlert(anyString(), anyString(), anyLong(), anyLong(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("Out of stock inventory triggers OUT_OF_STOCK alert")
        void outOfStockTriggersAlert() {
            when(inventoryRepository.findById(3L)).thenReturn(Optional.of(outOfStockInventory));
            when(thresholdRepository.findByProductIdAndWarehouseId(100L, 1L))
                    .thenReturn(Optional.of(threshold));
            when(alertLogRepository.save(any(InventoryAlertLog.class)))
                    .thenAnswer(i -> {
                        InventoryAlertLog log = i.getArgument(0);
                        log.setId(3L);
                        return log;
                    });

            InventoryAlertDTO result = alertService.checkAndAlert(3L);

            assertNotNull(result);
            assertEquals("OUT_OF_STOCK", result.getAlertType());
            assertEquals(0, result.getCurrentQuantity());
            assertEquals(0, result.getThresholdValue());
            verify(emailService).sendOutOfStockAlert(anyString(), anyString(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("Normal stock inventory does not trigger alert")
        void normalStockNoAlert() {
            when(inventoryRepository.findById(4L)).thenReturn(Optional.of(normalInventory));
            when(thresholdRepository.findByProductIdAndWarehouseId(100L, 1L))
                    .thenReturn(Optional.of(threshold));

            InventoryAlertDTO result = alertService.checkAndAlert(4L);

            assertNull(result);
            verify(emailService, never()).sendLowStockAlert(anyString(), anyString(), anyLong(), anyLong(), anyInt(), anyInt());
            verify(emailService, never()).sendCriticalStockAlert(anyString(), anyString(), anyLong(), anyLong(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("Inventory below reorder point triggers REORDER_POINT alert")
        void reorderPointTriggersAlert() {
            threshold = createThreshold(100L, 1L, 20, 5, 25, "admin@example.com", true);
            Inventory reorderInventory = createInventory(5L, 100L, 1L, 22);
            when(inventoryRepository.findById(5L)).thenReturn(Optional.of(reorderInventory));
            when(thresholdRepository.findByProductIdAndWarehouseId(100L, 1L))
                    .thenReturn(Optional.of(threshold));
            when(alertLogRepository.save(any(InventoryAlertLog.class)))
                    .thenAnswer(i -> {
                        InventoryAlertLog log = i.getArgument(0);
                        log.setId(5L);
                        return log;
                    });

            InventoryAlertDTO result = alertService.checkAndAlert(5L);

            assertNotNull(result);
            assertEquals("REORDER_POINT", result.getAlertType());
            verify(emailService).sendReorderAlert(anyString(), anyString(), anyLong(), anyLong(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("No threshold configured returns null")
        void noThresholdReturnsNull() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(lowStockInventory));
            when(thresholdRepository.findByProductIdAndWarehouseId(100L, 1L))
                    .thenReturn(Optional.empty());

            InventoryAlertDTO result = alertService.checkAndAlert(1L);

            assertNull(result);
            verify(alertLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("Disabled threshold returns null")
        void disabledThresholdReturnsNull() {
            threshold.setEnabled(false);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(lowStockInventory));
            when(thresholdRepository.findByProductIdAndWarehouseId(100L, 1L))
                    .thenReturn(Optional.of(threshold));

            InventoryAlertDTO result = alertService.checkAndAlert(1L);

            assertNull(result);
            verify(alertLogRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Check All Inventories Tests")
    class CheckAndAlertAllTests {

        @Test
        @DisplayName("Check all inventories returns alerts for problematic items")
        void checkAllReturnsAlerts() {
            List<Inventory> inventories = Arrays.asList(
                    lowStockInventory,
                    normalInventory,
                    outOfStockInventory
            );

            when(inventoryRepository.findAll()).thenReturn(inventories);
            when(thresholdRepository.findByProductIdAndWarehouseId(100L, 1L))
                    .thenReturn(Optional.of(threshold));
            when(alertLogRepository.save(any(InventoryAlertLog.class)))
                    .thenAnswer(i -> {
                        InventoryAlertLog log = i.getArgument(0);
                        log.setId(System.nanoTime());
                        return log;
                    });

            List<InventoryAlertDTO> results = alertService.checkAndAlertAll();

            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("Check all handles individual failures gracefully")
        void checkAllHandlesFailures() {
            Inventory failingInventory = createInventory(99L, 100L, 1L, 100);

            when(inventoryRepository.findAll()).thenReturn(Arrays.asList(lowStockInventory, failingInventory));
            when(thresholdRepository.findByProductIdAndWarehouseId(100L, 1L))
                    .thenReturn(Optional.of(threshold));
            when(alertLogRepository.save(any(InventoryAlertLog.class)))
                    .thenAnswer(i -> {
                        InventoryAlertLog log = i.getArgument(0);
                        log.setId(System.nanoTime());
                        return log;
                    });

            List<InventoryAlertDTO> results = alertService.checkAndAlertAll();

            assertEquals(1, results.size());
        }
    }

    @Nested
    @DisplayName("Threshold Management Tests")
    class ThresholdManagementTests {

        @Test
        @DisplayName("Create new threshold")
        void createThreshold() {
            InventoryAlertThresholdDTO dto = InventoryAlertThresholdDTO.builder()
                    .productId(200L)
                    .warehouseId(2L)
                    .lowStockThreshold(30)
                    .criticalStockThreshold(10)
                    .reorderPoint(15)
                    .alertEmail("new@example.com")
                    .enabled(true)
                    .build();

            when(thresholdRepository.findByProductIdAndWarehouseId(200L, 2L))
                    .thenReturn(Optional.empty());
            when(thresholdRepository.save(any(InventoryAlertThreshold.class)))
                    .thenAnswer(i -> {
                        InventoryAlertThreshold t = i.getArgument(0);
                        t.setId(10L);
                        return t;
                    });

            InventoryAlertThresholdDTO result = alertService.createOrUpdateThreshold(dto);

            assertNotNull(result);
            assertEquals(10L, result.getId());
            verify(thresholdRepository).save(any(InventoryAlertThreshold.class));
        }

        @Test
        @DisplayName("Update existing threshold")
        void updateThreshold() {
            InventoryAlertThresholdDTO dto = InventoryAlertThresholdDTO.builder()
                    .productId(100L)
                    .warehouseId(1L)
                    .lowStockThreshold(50)
                    .criticalStockThreshold(15)
                    .reorderPoint(25)
                    .alertEmail("updated@example.com")
                    .enabled(true)
                    .build();

            when(thresholdRepository.findByProductIdAndWarehouseId(100L, 1L))
                    .thenReturn(Optional.of(threshold));
            when(thresholdRepository.save(any(InventoryAlertThreshold.class)))
                    .thenAnswer(i -> i.getArgument(0));

            InventoryAlertThresholdDTO result = alertService.createOrUpdateThreshold(dto);

            assertNotNull(result);
            assertEquals(50, result.getLowStockThreshold());
            verify(thresholdRepository).save(any(InventoryAlertThreshold.class));
        }

        @Test
        @DisplayName("Get all thresholds")
        void getAllThresholds() {
            when(thresholdRepository.findAll()).thenReturn(Arrays.asList(threshold));

            List<InventoryAlertThresholdDTO> results = alertService.getAllThresholds();

            assertEquals(1, results.size());
        }
    }

    @Nested
    @DisplayName("Alert Acknowledgment Tests")
    class AlertAcknowledgmentTests {

        @Test
        @DisplayName("Acknowledge alert successfully")
        void acknowledgeAlert() {
            InventoryAlertLog alertLog = new InventoryAlertLog();
            alertLog.setId(1L);
            alertLog.setAcknowledged(false);

            when(alertLogRepository.findById(1L)).thenReturn(Optional.of(alertLog));
            when(alertLogRepository.save(any(InventoryAlertLog.class)))
                    .thenAnswer(i -> i.getArgument(0));

            alertService.acknowledgeAlert(1L, "admin");

            ArgumentCaptor<InventoryAlertLog> captor = ArgumentCaptor.forClass(InventoryAlertLog.class);
            verify(alertLogRepository).save(captor.capture());

            InventoryAlertLog saved = captor.getValue();
            assertTrue(saved.isAcknowledged());
            assertEquals("admin", saved.getAcknowledgedBy());
            assertNotNull(saved.getAcknowledgedAt());
        }

        @Test
        @DisplayName("Get unacknowledged alerts")
        void getUnacknowledgedAlerts() {
            InventoryAlertLog alert = new InventoryAlertLog();
            alert.setId(1L);
            alert.setAcknowledged(false);

            when(alertLogRepository.findByAcknowledged(false))
                    .thenReturn(Arrays.asList(alert));

            List<InventoryAlertLog> results = alertService.getUnacknowledgedAlerts();

            assertEquals(1, results.size());
        }
    }

    @Nested
    @DisplayName("Alert Email Tests")
    class AlertEmailTests {

        @Test
        @DisplayName("Alert email is saved with SENT status")
        void alertEmailSavedWithSentStatus() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(lowStockInventory));
            when(thresholdRepository.findByProductIdAndWarehouseId(100L, 1L))
                    .thenReturn(Optional.of(threshold));
            when(alertLogRepository.save(any(InventoryAlertLog.class)))
                    .thenAnswer(i -> {
                        InventoryAlertLog log = i.getArgument(0);
                        log.setId(1L);
                        return log;
                    });

            alertService.checkAndAlert(1L);

            ArgumentCaptor<InventoryAlertLog> captor = ArgumentCaptor.forClass(InventoryAlertLog.class);
            verify(alertLogRepository).save(captor.capture());

            InventoryAlertLog saved = captor.getValue();
            assertEquals("SENT", saved.getEmailStatus());
            assertEquals("admin@example.com", saved.getEmailSentTo());
        }

        @Test
        @DisplayName("Alert without email is saved with PENDING status")
        void alertWithoutEmailSavedWithPendingStatus() {
            threshold.setAlertEmail(null);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(lowStockInventory));
            when(thresholdRepository.findByProductIdAndWarehouseId(100L, 1L))
                    .thenReturn(Optional.of(threshold));
            when(alertLogRepository.save(any(InventoryAlertLog.class)))
                    .thenAnswer(i -> {
                        InventoryAlertLog log = i.getArgument(0);
                        log.setId(1L);
                        return log;
                    });

            alertService.checkAndAlert(1L);

            ArgumentCaptor<InventoryAlertLog> captor = ArgumentCaptor.forClass(InventoryAlertLog.class);
            verify(alertLogRepository).save(captor.capture());

            InventoryAlertLog saved = captor.getValue();
            assertEquals("PENDING", saved.getEmailStatus());
            verify(emailService, never()).sendLowStockAlert(anyString(), anyString(), anyLong(), anyLong(), anyInt(), anyInt());
        }
    }
}
