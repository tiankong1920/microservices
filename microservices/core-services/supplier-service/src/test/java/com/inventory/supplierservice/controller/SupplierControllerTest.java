package com.inventory.supplierservice.controller;

import com.inventory.supplierservice.dto.SupplierDTO;
import com.inventory.supplierservice.service.ISupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class SupplierControllerTest {

    @Mock
    private ISupplierService supplierService;

    @InjectMocks
    private SupplierController supplierController;

    private SupplierDTO testSupplier;

    @BeforeEach
    void setUp() {
        testSupplier = new SupplierDTO();
        testSupplier.setId(1L);
        testSupplier.setSupplierCode("SUP-001");
        testSupplier.setSupplierName("Acme Supplies");
        testSupplier.setActive(true);
    }

    @Test
    void testGetAllSuppliersPaged() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<SupplierDTO> page = new PageImpl<>(List.of(testSupplier), pageable, 1);
        when(supplierService.getAllSuppliers(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<SupplierDTO>> response = supplierController.getAllSuppliers(0, 20, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void testGetAllSuppliersPagedDesc() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<SupplierDTO> page = new PageImpl<>(List.of(testSupplier), pageable, 1);
        when(supplierService.getAllSuppliers(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<SupplierDTO>> response = supplierController.getAllSuppliers(0, 20, "id", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetAllSuppliersList() {
        when(supplierService.getAllSuppliers()).thenReturn(List.of(testSupplier));

        ResponseEntity<List<SupplierDTO>> response = supplierController.getAllSuppliersList();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetSupplierById() {
        when(supplierService.getSupplierById(1L)).thenReturn(testSupplier);

        ResponseEntity<SupplierDTO> response = supplierController.getSupplierById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SUP-001", response.getBody().getSupplierCode());
    }

    @Test
    void testGetActiveSuppliers() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<SupplierDTO> page = new PageImpl<>(List.of(testSupplier), pageable, 1);
        when(supplierService.getActiveSuppliers(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<SupplierDTO>> response = supplierController.getActiveSuppliers(0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void testGetSuppliersByCategory() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<SupplierDTO> page = new PageImpl<>(List.of(testSupplier), pageable, 1);
        when(supplierService.getSuppliersByProductCategory(anyString(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<SupplierDTO>> response = supplierController.getSuppliersByCategory("ELECTRONICS", 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testSearchSuppliers() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<SupplierDTO> page = new PageImpl<>(List.of(testSupplier), pageable, 1);
        when(supplierService.searchSuppliers(anyString(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<SupplierDTO>> response = supplierController.searchSuppliers("Acme", 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void testSearchSuppliersByName() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<SupplierDTO> page = new PageImpl<>(List.of(testSupplier), pageable, 1);
        when(supplierService.searchSuppliersByName(anyString(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<SupplierDTO>> response = supplierController.searchSuppliersByName("Acme", 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateSupplier() {
        SupplierDTO input = new SupplierDTO();
        input.setSupplierName("New Supplier");
        input.setSupplierCode("SUP-002");

        SupplierDTO created = new SupplierDTO();
        created.setId(2L);
        created.setSupplierName("New Supplier");
        created.setSupplierCode("SUP-002");

        when(supplierService.createSupplier(any(SupplierDTO.class))).thenReturn(created);

        ResponseEntity<SupplierDTO> response = supplierController.createSupplier(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(2L, response.getBody().getId());
    }

    @Test
    void testUpdateSupplier() {
        SupplierDTO input = new SupplierDTO();
        input.setSupplierName("Updated Supplier");

        SupplierDTO updated = new SupplierDTO();
        updated.setId(1L);
        updated.setSupplierName("Updated Supplier");

        when(supplierService.updateSupplier(anyLong(), any(SupplierDTO.class))).thenReturn(updated);

        ResponseEntity<SupplierDTO> response = supplierController.updateSupplier(1L, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Supplier", response.getBody().getSupplierName());
    }

    @Test
    void testDeleteSupplier() {
        doNothing().when(supplierService).deleteSupplier(1L);

        ResponseEntity<Void> response = supplierController.deleteSupplier(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(supplierService).deleteSupplier(1L);
    }

    @Test
    void testActivateSupplier() {
        when(supplierService.activateSupplier(1L)).thenReturn(testSupplier);

        ResponseEntity<SupplierDTO> response = supplierController.activateSupplier(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeactivateSupplier() {
        SupplierDTO deactivated = new SupplierDTO();
        deactivated.setId(1L);
        deactivated.setActive(false);
        when(supplierService.deactivateSupplier(1L)).thenReturn(deactivated);

        ResponseEntity<SupplierDTO> response = supplierController.deactivateSupplier(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(false, response.getBody().getActive());
    }
}
