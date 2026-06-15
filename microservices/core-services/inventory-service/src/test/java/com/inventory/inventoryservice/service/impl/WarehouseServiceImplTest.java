package com.inventory.inventoryservice.service.impl;

import com.inventory.inventoryservice.dto.WarehouseDTO;
import com.inventory.inventoryservice.entity.Warehouse;
import com.inventory.inventoryservice.exception.WarehouseNotFoundException;
import com.inventory.inventoryservice.repository.IWarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class WarehouseServiceImplTest {

    @Mock
    private IWarehouseRepository warehouseRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    private Warehouse testWarehouse;
    private WarehouseDTO testWarehouseDTO;
    private WarehouseDTO updatedWarehouseDTO;
    private Warehouse updatedWarehouse;

    private static final Long NON_EXISTENT_WAREHOUSE_ID = 999L;
    private static final int UPDATED_CAPACITY = 2000;

    @BeforeEach
    void setUp() {
        setUpTestWarehouse();
        setUpTestWarehouseDTO();
        setUpUpdatedWarehouseDTO();
        setUpUpdatedWarehouse();
    }

    private void setUpTestWarehouse() {
        testWarehouse = new Warehouse();
        testWarehouse.setId(1L);
        testWarehouse.setWarehouseCode("WH-001");
        testWarehouse.setWarehouseName("Test Warehouse");
        testWarehouse.setAddress("123 Warehouse Street");
        testWarehouse.setCity("Test City");
        testWarehouse.setProvince("Test Province");
        testWarehouse.setCountry("Test Country");
        testWarehouse.setPostalCode("12345");
        testWarehouse.setContactPhone("1234567890");
        testWarehouse.setContactEmail("test@warehouse.com");
        testWarehouse.setContactPerson("John Doe");
        testWarehouse.setCapacity(1000);
        testWarehouse.setActive(true);
    }

    private void setUpTestWarehouseDTO() {
        testWarehouseDTO = new WarehouseDTO();
        testWarehouseDTO.setId(1L);
        testWarehouseDTO.setWarehouseCode("WH-001");
        testWarehouseDTO.setWarehouseName("Test Warehouse");
        testWarehouseDTO.setAddress("123 Warehouse Street");
        testWarehouseDTO.setCity("Test City");
        testWarehouseDTO.setProvince("Test Province");
        testWarehouseDTO.setCountry("Test Country");
        testWarehouseDTO.setPostalCode("12345");
        testWarehouseDTO.setContactPhone("1234567890");
        testWarehouseDTO.setContactEmail("test@warehouse.com");
        testWarehouseDTO.setContactPerson("John Doe");
        testWarehouseDTO.setCapacity(1000);
        testWarehouseDTO.setIsActive(true);
        testWarehouseDTO.setIsPrimary(false);
    }

    private void setUpUpdatedWarehouseDTO() {
        updatedWarehouseDTO = new WarehouseDTO();
        updatedWarehouseDTO.setId(1L);
        updatedWarehouseDTO.setWarehouseCode("WH-001-UPDATED");
        updatedWarehouseDTO.setWarehouseName("Updated Warehouse");
        updatedWarehouseDTO.setAddress("456 Updated Street");
        updatedWarehouseDTO.setCity("Updated City");
        updatedWarehouseDTO.setProvince("Updated Province");
        updatedWarehouseDTO.setCountry("Updated Country");
        updatedWarehouseDTO.setPostalCode("54321");
        updatedWarehouseDTO.setContactPhone("9876543210");
        updatedWarehouseDTO.setContactEmail("updated@warehouse.com");
        updatedWarehouseDTO.setContactPerson("Jane Doe");
        updatedWarehouseDTO.setCapacity(UPDATED_CAPACITY);
        updatedWarehouseDTO.setIsActive(true);
        updatedWarehouseDTO.setIsPrimary(true);
    }

    private void setUpUpdatedWarehouse() {
        updatedWarehouse = new Warehouse();
        updatedWarehouse.setId(1L);
        updatedWarehouse.setWarehouseCode("WH-001-UPDATED");
        updatedWarehouse.setWarehouseName("Updated Warehouse");
        updatedWarehouse.setAddress("456 Updated Street");
        updatedWarehouse.setCity("Updated City");
        updatedWarehouse.setProvince("Updated Province");
        updatedWarehouse.setCountry("Updated Country");
        updatedWarehouse.setPostalCode("54321");
        updatedWarehouse.setContactPhone("9876543210");
        updatedWarehouse.setContactEmail("updated@warehouse.com");
        updatedWarehouse.setContactPerson("Jane Doe");
        updatedWarehouse.setCapacity(UPDATED_CAPACITY);
        updatedWarehouse.setActive(true);
    }




    @Test
    void testGetAllWarehouses() {
        when(warehouseRepository.findAll()).thenReturn(Arrays.asList(testWarehouse));
        when(modelMapper.map(testWarehouse, WarehouseDTO.class)).thenReturn(testWarehouseDTO);

        final List<WarehouseDTO> result = warehouseService.getAllWarehouses();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("WH-001", result.get(0).getWarehouseCode());
        verify(warehouseRepository, times(1)).findAll();
    }

    @Test
    void testGetWarehouseById() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(modelMapper.map(testWarehouse, WarehouseDTO.class)).thenReturn(testWarehouseDTO);

        final WarehouseDTO result = warehouseService.getWarehouseById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("WH-001", result.getWarehouseCode());
        verify(warehouseRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(testWarehouse, WarehouseDTO.class);
    }

    @Test
    void testGetWarehouseByIdNotFound() {
        when(warehouseRepository.findById(NON_EXISTENT_WAREHOUSE_ID)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () -> {
            warehouseService.getWarehouseById(NON_EXISTENT_WAREHOUSE_ID);
        });

        verify(warehouseRepository, times(1)).findById(NON_EXISTENT_WAREHOUSE_ID);
    }

    @Test
    void testGetWarehouseByCode() {
        when(warehouseRepository.findByWarehouseCode("WH-001")).thenReturn(Optional.of(testWarehouse));
        when(modelMapper.map(testWarehouse, WarehouseDTO.class)).thenReturn(testWarehouseDTO);

        final WarehouseDTO result = warehouseService.getWarehouseByCode("WH-001");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("WH-001", result.getWarehouseCode());
        verify(warehouseRepository, times(1)).findByWarehouseCode("WH-001");
        verify(modelMapper, times(1)).map(testWarehouse, WarehouseDTO.class);
    }

    @Test
    void testGetWarehouseByCodeNotFound() {
        when(warehouseRepository.findByWarehouseCode("NOTFOUND")).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () -> {
            warehouseService.getWarehouseByCode("NOTFOUND");
        });

        verify(warehouseRepository, times(1)).findByWarehouseCode("NOTFOUND");
    }

    @Test
    void testGetWarehousesByCity() {
        when(warehouseRepository.findByCity("Test City")).thenReturn(Arrays.asList(testWarehouse));
        when(modelMapper.map(testWarehouse, WarehouseDTO.class)).thenReturn(testWarehouseDTO);

        final List<WarehouseDTO> result = warehouseService.getWarehousesByCity("Test City");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test City", result.get(0).getCity());
        verify(warehouseRepository, times(1)).findByCity("Test City");
        verify(modelMapper, times(1)).map(testWarehouse, WarehouseDTO.class);
    }

    @Test
    void testGetWarehousesByProvince() {
        when(warehouseRepository.findByProvince("Test Province")).thenReturn(Arrays.asList(testWarehouse));
        when(modelMapper.map(testWarehouse, WarehouseDTO.class)).thenReturn(testWarehouseDTO);

        final List<WarehouseDTO> result = warehouseService.getWarehousesByProvince("Test Province");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Province", result.get(0).getProvince());
        verify(warehouseRepository, times(1)).findByProvince("Test Province");
        verify(modelMapper, times(1)).map(testWarehouse, WarehouseDTO.class);
    }

    @Test
    void testGetWarehousesByCountry() {
        when(warehouseRepository.findByCountry("Test Country")).thenReturn(Arrays.asList(testWarehouse));
        when(modelMapper.map(testWarehouse, WarehouseDTO.class)).thenReturn(testWarehouseDTO);

        final List<WarehouseDTO> result = warehouseService.getWarehousesByCountry("Test Country");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Country", result.get(0).getCountry());
        verify(warehouseRepository, times(1)).findByCountry("Test Country");
        verify(modelMapper, times(1)).map(testWarehouse, WarehouseDTO.class);
    }

    @Test
    void testGetActiveWarehouses() {
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(testWarehouse));
        when(modelMapper.map(testWarehouse, WarehouseDTO.class)).thenReturn(testWarehouseDTO);

        final List<WarehouseDTO> result = warehouseService.getActiveWarehouses();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(true, result.get(0).getIsActive());
        verify(warehouseRepository, times(1)).findByIsActiveTrue();
        verify(modelMapper, times(1)).map(testWarehouse, WarehouseDTO.class);
    }

    @Test
    void testGetPrimaryWarehouses() {
        when(warehouseRepository.findByIsPrimaryTrue()).thenReturn(Arrays.asList(testWarehouse));
        when(modelMapper.map(testWarehouse, WarehouseDTO.class)).thenReturn(testWarehouseDTO);

        final List<WarehouseDTO> result = warehouseService.getPrimaryWarehouses();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(false, result.get(0).getIsPrimary());
        verify(warehouseRepository, times(1)).findByIsPrimaryTrue();
        verify(modelMapper, times(1)).map(testWarehouse, WarehouseDTO.class);
    }

    @Test
    void testCreateWarehouse() {
        when(modelMapper.map(testWarehouseDTO, Warehouse.class)).thenReturn(testWarehouse);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);
        when(modelMapper.map(testWarehouse, WarehouseDTO.class)).thenReturn(testWarehouseDTO);

        final WarehouseDTO result = warehouseService.createWarehouse(testWarehouseDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("WH-001", result.getWarehouseCode());
        assertEquals("Test Warehouse", result.getWarehouseName());
        assertEquals(1000, result.getCapacity());
        assertEquals(true, result.getIsActive());
        assertEquals(false, result.getIsPrimary());

        verify(modelMapper, times(1)).map(testWarehouseDTO, Warehouse.class);
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
        verify(modelMapper, times(1)).map(testWarehouse, WarehouseDTO.class);
    }

    @Test
    void testCreateWarehouseWarehouseCodeExists() {
        when(warehouseRepository.existsByWarehouseCode("WH-001")).thenReturn(true);

        final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            warehouseService.createWarehouse(testWarehouseDTO);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(warehouseRepository, times(1)).existsByWarehouseCode("WH-001");
        verify(warehouseRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testUpdateWarehouse() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(updatedWarehouse);
        // Mock the modelMapper.map(warehouseDTO, existingWarehouse) call
        doNothing().when(modelMapper).map(any(WarehouseDTO.class), any(Warehouse.class));
        when(modelMapper.map(updatedWarehouse, WarehouseDTO.class)).thenReturn(updatedWarehouseDTO);

        final WarehouseDTO result = warehouseService.updateWarehouse(1L, updatedWarehouseDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("WH-001-UPDATED", result.getWarehouseCode());
        assertEquals("Updated Warehouse", result.getWarehouseName());
        assertEquals(UPDATED_CAPACITY, result.getCapacity());
        assertEquals(true, result.getIsActive());
        assertEquals(true, result.getIsPrimary());

        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
        verify(modelMapper, times(1)).map(any(WarehouseDTO.class), any(Warehouse.class));
        verify(modelMapper, times(1)).map(updatedWarehouse, WarehouseDTO.class);
    }

    @Test
    void testUpdateWarehouseNotFound() {
        when(warehouseRepository.findById(NON_EXISTENT_WAREHOUSE_ID)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () -> {
            warehouseService.updateWarehouse(NON_EXISTENT_WAREHOUSE_ID, updatedWarehouseDTO);
        });

        verify(warehouseRepository, times(1)).findById(NON_EXISTENT_WAREHOUSE_ID);
        verify(warehouseRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testUpdateWarehouseWarehouseCodeChanged() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(warehouseRepository.existsByWarehouseCode("WH-001-UPDATED")).thenReturn(true);

        final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            warehouseService.updateWarehouse(1L, updatedWarehouseDTO);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).existsByWarehouseCode("WH-001-UPDATED");
        verify(warehouseRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testDeleteWarehouse() {
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        doNothing().when(warehouseRepository).deleteById(1L);

        warehouseService.deleteWarehouse(1L);

        verify(warehouseRepository, times(1)).existsById(1L);
        verify(warehouseRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteWarehouseNotFound() {
        when(warehouseRepository.existsById(NON_EXISTENT_WAREHOUSE_ID)).thenReturn(false);

        assertThrows(WarehouseNotFoundException.class, () -> {
            warehouseService.deleteWarehouse(NON_EXISTENT_WAREHOUSE_ID);
        });

        verify(warehouseRepository, times(1)).existsById(NON_EXISTENT_WAREHOUSE_ID);
        verify(warehouseRepository, never()).deleteById(any());
    }

    @Test
    void testActivateWarehouse() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(warehouse -> {
            final Warehouse wh = warehouse.getArgument(0);
            wh.setActive(true);
            return wh;
        });

        warehouseService.activateWarehouse(1L);

        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    void testActivateWarehouseNotFound() {
        when(warehouseRepository.findById(NON_EXISTENT_WAREHOUSE_ID)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () -> {
            warehouseService.activateWarehouse(NON_EXISTENT_WAREHOUSE_ID);
        });

        verify(warehouseRepository, times(1)).findById(NON_EXISTENT_WAREHOUSE_ID);
        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void testDeactivateWarehouse() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(warehouse -> {
            final Warehouse wh = warehouse.getArgument(0);
            wh.setActive(false);
            return wh;
        });

        warehouseService.deactivateWarehouse(1L);

        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    void testDeactivateWarehouseNotFound() {
        when(warehouseRepository.findById(NON_EXISTENT_WAREHOUSE_ID)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () -> {
            warehouseService.deactivateWarehouse(NON_EXISTENT_WAREHOUSE_ID);
        });

        verify(warehouseRepository, times(1)).findById(NON_EXISTENT_WAREHOUSE_ID);
        verify(warehouseRepository, never()).save(any());
    }

}
