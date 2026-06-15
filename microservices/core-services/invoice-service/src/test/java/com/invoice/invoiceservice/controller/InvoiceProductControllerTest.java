package com.invoice.invoiceservice.controller;

import com.invoice.invoiceservice.dto.InvoiceProductDTO;
import com.invoice.invoiceservice.service.InvoiceProductService;
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
class InvoiceProductControllerTest {

    @Mock
    private InvoiceProductService productService;

    @InjectMocks
    private InvoiceProductController invoiceProductController;

    private InvoiceProductDTO testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new InvoiceProductDTO();
        testProduct.setId(1L);
        testProduct.setProductName("Test Product");
        testProduct.setSpecification("P-001");
    }

    @Test
    void testCreateProduct() {
        when(productService.createProduct(any(InvoiceProductDTO.class))).thenReturn(testProduct);

        ResponseEntity<InvoiceProductDTO> response = invoiceProductController.createProduct(testProduct);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Product", response.getBody().getProductName());
    }

    @Test
    void testUpdateProduct() {
        when(productService.updateProduct(eq(1L), any(InvoiceProductDTO.class))).thenReturn(testProduct);

        ResponseEntity<InvoiceProductDTO> response = invoiceProductController.updateProduct(1L, testProduct);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetProductById() {
        when(productService.getProductById(1L)).thenReturn(testProduct);

        ResponseEntity<InvoiceProductDTO> response = invoiceProductController.getProductById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("P-001", response.getBody().getSpecification());
    }

    @Test
    void testGetAllProducts() {
        when(productService.getAllProducts()).thenReturn(List.of(testProduct));

        ResponseEntity<List<InvoiceProductDTO>> response = invoiceProductController.getAllProducts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetActiveProducts() {
        when(productService.getActiveProducts()).thenReturn(List.of(testProduct));

        ResponseEntity<List<InvoiceProductDTO>> response = invoiceProductController.getActiveProducts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteProduct() {
        doNothing().when(productService).deleteProduct(1L);

        ResponseEntity<Void> response = invoiceProductController.deleteProduct(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productService).deleteProduct(1L);
    }

    @Test
    void testSearchProducts() {
        when(productService.searchProducts("test")).thenReturn(List.of(testProduct));

        ResponseEntity<List<InvoiceProductDTO>> response = invoiceProductController.searchProducts("test");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetFrequentProducts() {
        when(productService.getFrequentlyUsedProducts(5)).thenReturn(List.of(testProduct));

        ResponseEntity<List<InvoiceProductDTO>> response = invoiceProductController.getFrequentProducts(5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testRecommendProducts() {
        when(productService.recommendProductsByCustomer(100L, 10)).thenReturn(List.of(testProduct));

        ResponseEntity<List<InvoiceProductDTO>> response = invoiceProductController.recommendProducts(100L, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }
}
