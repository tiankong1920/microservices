package com.invoice.invoiceservice.controller;

import com.invoice.invoiceservice.dto.CustomerInfoDTO;
import com.invoice.invoiceservice.service.CustomerInfoService;
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
class CustomerInfoControllerTest {

    @Mock
    private CustomerInfoService customerInfoService;

    @InjectMocks
    private CustomerInfoController customerInfoController;

    private CustomerInfoDTO testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new CustomerInfoDTO();
        testCustomer.setId(1L);
        testCustomer.setCustomerName("Acme Corp");
        testCustomer.setTaxNumber("TAX-001");
    }

    @Test
    void testCreateCustomer() {
        when(customerInfoService.createCustomer(any(CustomerInfoDTO.class))).thenReturn(testCustomer);

        ResponseEntity<CustomerInfoDTO> response = customerInfoController.createCustomer(testCustomer);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Acme Corp", response.getBody().getCustomerName());
    }

    @Test
    void testUpdateCustomer() {
        when(customerInfoService.updateCustomer(eq(1L), any(CustomerInfoDTO.class))).thenReturn(testCustomer);

        ResponseEntity<CustomerInfoDTO> response = customerInfoController.updateCustomer(1L, testCustomer);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetCustomerById() {
        when(customerInfoService.getCustomerById(1L)).thenReturn(testCustomer);

        ResponseEntity<CustomerInfoDTO> response = customerInfoController.getCustomerById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TAX-001", response.getBody().getTaxNumber());
    }

    @Test
    void testGetAllCustomers() {
        when(customerInfoService.getAllCustomers()).thenReturn(List.of(testCustomer));

        ResponseEntity<List<CustomerInfoDTO>> response = customerInfoController.getAllCustomers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetActiveCustomers() {
        when(customerInfoService.getActiveCustomers()).thenReturn(List.of(testCustomer));

        ResponseEntity<List<CustomerInfoDTO>> response = customerInfoController.getActiveCustomers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteCustomer() {
        doNothing().when(customerInfoService).deleteCustomer(1L);

        ResponseEntity<Void> response = customerInfoController.deleteCustomer(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(customerInfoService).deleteCustomer(1L);
    }

    @Test
    void testEnableCustomer() {
        doNothing().when(customerInfoService).enableCustomer(1L);

        ResponseEntity<Void> response = customerInfoController.enableCustomer(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(customerInfoService).enableCustomer(1L);
    }

    @Test
    void testDisableCustomer() {
        doNothing().when(customerInfoService).disableCustomer(1L);

        ResponseEntity<Void> response = customerInfoController.disableCustomer(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(customerInfoService).disableCustomer(1L);
    }

    @Test
    void testSearchCustomers() {
        when(customerInfoService.searchCustomers("acme")).thenReturn(List.of(testCustomer));

        ResponseEntity<List<CustomerInfoDTO>> response = customerInfoController.searchCustomers("acme");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testAutoFillCustomerInfo() {
        when(customerInfoService.autoFillCustomerInfo(1L)).thenReturn(testCustomer);

        ResponseEntity<CustomerInfoDTO> response = customerInfoController.autoFillCustomerInfo(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Acme Corp", response.getBody().getCustomerName());
    }
}
