package com.inventory.inventoryservice.controller;

import com.inventory.inventoryservice.dto.OtherStockInOrderDTO;
import com.inventory.inventoryservice.dto.OtherStockOutOrderDTO;
import com.inventory.inventoryservice.service.IOtherStockService;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class OtherStockControllerTest {

    @Mock
    private IOtherStockService otherStockService;

    @InjectMocks
    private OtherStockController otherStockController;

    private OtherStockInOrderDTO testInOrder;
    private OtherStockOutOrderDTO testOutOrder;

    @BeforeEach
    void setUp() {
        testInOrder = new OtherStockInOrderDTO();
        testInOrder.setId(1L);
        testInOrder.setOrderNumber("IN-001");

        testOutOrder = new OtherStockOutOrderDTO();
        testOutOrder.setId(2L);
        testOutOrder.setOrderNumber("OUT-001");
    }

    @Test
    void testGetAllOtherStockInOrders() {
        when(otherStockService.getAllOtherStockInOrders()).thenReturn(List.of(testInOrder));

        ResponseEntity<List<OtherStockInOrderDTO>> response =
                otherStockController.getAllOtherStockInOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetAllOtherStockOutOrders() {
        when(otherStockService.getAllOtherStockOutOrders()).thenReturn(List.of(testOutOrder));

        ResponseEntity<List<OtherStockOutOrderDTO>> response =
                otherStockController.getAllOtherStockOutOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetOtherStockInOrderById() {
        when(otherStockService.getOtherStockInOrderById(1L)).thenReturn(testInOrder);

        ResponseEntity<OtherStockInOrderDTO> response = otherStockController.getOtherStockInOrderById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("IN-001", response.getBody().getOrderNumber());
    }

    @Test
    void testGetOtherStockOutOrderById() {
        when(otherStockService.getOtherStockOutOrderById(2L)).thenReturn(testOutOrder);

        ResponseEntity<OtherStockOutOrderDTO> response = otherStockController.getOtherStockOutOrderById(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OUT-001", response.getBody().getOrderNumber());
    }

    @Test
    void testCreateOtherStockInOrder() {
        OtherStockInOrderDTO input = new OtherStockInOrderDTO();
        input.setOrderNumber("IN-002");

        OtherStockInOrderDTO created = new OtherStockInOrderDTO();
        created.setId(3L);
        created.setOrderNumber("IN-002");

        when(otherStockService.createOtherStockInOrder(any(OtherStockInOrderDTO.class))).thenReturn(created);

        ResponseEntity<OtherStockInOrderDTO> response = otherStockController.createOtherStockInOrder(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3L, response.getBody().getId());
    }

    @Test
    void testCreateOtherStockOutOrder() {
        OtherStockOutOrderDTO input = new OtherStockOutOrderDTO();
        input.setOrderNumber("OUT-002");

        OtherStockOutOrderDTO created = new OtherStockOutOrderDTO();
        created.setId(4L);
        created.setOrderNumber("OUT-002");

        when(otherStockService.createOtherStockOutOrder(any(OtherStockOutOrderDTO.class))).thenReturn(created);

        ResponseEntity<OtherStockOutOrderDTO> response = otherStockController.createOtherStockOutOrder(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(4L, response.getBody().getId());
    }

    @Test
    void testDeleteOtherStockInOrder() {
        doNothing().when(otherStockService).deleteOtherStockInOrder(1L);

        ResponseEntity<Void> response = otherStockController.deleteOtherStockInOrder(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(otherStockService).deleteOtherStockInOrder(1L);
    }

    @Test
    void testDeleteOtherStockOutOrder() {
        doNothing().when(otherStockService).deleteOtherStockOutOrder(2L);

        ResponseEntity<Void> response = otherStockController.deleteOtherStockOutOrder(2L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(otherStockService).deleteOtherStockOutOrder(2L);
    }
}
