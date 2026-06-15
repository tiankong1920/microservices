package com.inventory.productservice.controller;

import com.inventory.productservice.dto.ProductDTO;
import com.inventory.productservice.dto.ProductSKUDTO;
import com.inventory.productservice.exception.ProductNotFoundException;
import com.inventory.productservice.service.ProductService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductDTO testProduct;
    private ProductSKUDTO testSku;

    @BeforeEach
    void setUp() {
        testProduct = new ProductDTO();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setSku("TEST-001");
        testProduct.setPrice(new BigDecimal("100.00"));
        testProduct.setStatus("ACTIVE");

        testSku = new ProductSKUDTO();
        testSku.setId(10L);
        testSku.setProductId(1L);
        testSku.setSkuCode("SKU-001");
        testSku.setPrice(new BigDecimal("99.99"));
    }

    @Test
    void testGetAllProducts() {
        when(productService.getAllProducts()).thenReturn(List.of(testProduct));

        ResponseEntity<List<ProductDTO>> response = productController.getAllProducts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Test Product", response.getBody().get(0).getName());
        verify(productService).getAllProducts();
    }

    @Test
    void testGetAllProductsEmpty() {
        when(productService.getAllProducts()).thenReturn(List.of());

        ResponseEntity<List<ProductDTO>> response = productController.getAllProducts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void testGetProductsPaged() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));
        Page<ProductDTO> page = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<ProductDTO>> response = productController.getProductsPaged(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("Test Product", response.getBody().getContent().get(0).getName());
    }

    @Test
    void testGetProductsPagedBlockedSortField() {
        Pageable unsafe = PageRequest.of(0, 20, Sort.by("password"));
        Pageable safe = PageRequest.of(0, 20);
        Page<ProductDTO> page = new PageImpl<>(List.of(testProduct), safe, 1);
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<ProductDTO>> response = productController.getProductsPaged(unsafe);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetProductById() {
        when(productService.getProductById(1L)).thenReturn(testProduct);

        ResponseEntity<ProductDTO> response = productController.getProductById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(productService).getProductById(1L);
    }

    @Test
    void testGetProductByIdNotFound() {
        when(productService.getProductById(999L))
                .thenThrow(new ProductNotFoundException("Product not found with id: 999"));

        assertThrows(ProductNotFoundException.class,
                () -> productController.getProductById(999L));
        verify(productService).getProductById(999L);
    }

    @Test
    void testCreateProduct() {
        ProductDTO input = new ProductDTO();
        input.setName("New Product");
        input.setSku("NEW-001");

        ProductDTO created = new ProductDTO();
        created.setId(2L);
        created.setName("New Product");
        created.setSku("NEW-001");
        created.setStatus("ACTIVE");

        when(productService.createProduct(any(ProductDTO.class))).thenReturn(created);

        ResponseEntity<ProductDTO> response = productController.createProduct(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().getId());
        verify(productService).createProduct(input);
    }

    @Test
    void testUpdateProduct() {
        ProductDTO input = new ProductDTO();
        input.setName("Updated Product");
        input.setPrice(new BigDecimal("150.00"));

        ProductDTO updated = new ProductDTO();
        updated.setId(1L);
        updated.setName("Updated Product");
        updated.setPrice(new BigDecimal("150.00"));

        when(productService.updateProduct(eq(1L), any(ProductDTO.class))).thenReturn(updated);

        ResponseEntity<ProductDTO> response = productController.updateProduct(1L, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Product", response.getBody().getName());
    }

    @Test
    void testDeleteProduct() {
        doNothing().when(productService).deleteProduct(1L);

        ResponseEntity<Void> response = productController.deleteProduct(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(productService).deleteProduct(1L);
    }

    @Test
    void testGetProductBySku() {
        when(productService.getProductBySku("TEST-001")).thenReturn(testProduct);

        ResponseEntity<ProductDTO> response = productController.getProductBySku("TEST-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TEST-001", response.getBody().getSku());
    }

    @Test
    void testGetProductSKUs() {
        when(productService.getProductSKUs(1L)).thenReturn(List.of(testSku));

        ResponseEntity<List<ProductSKUDTO>> response = productController.getProductSKUs(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("SKU-001", response.getBody().get(0).getSkuCode());
    }

    @Test
    void testGetProductSKUByCode() {
        when(productService.getProductSKUByCode("SKU-001")).thenReturn(testSku);

        ResponseEntity<ProductSKUDTO> response = productController.getProductSKUByCode("SKU-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getId());
    }

    @Test
    void testCreateProductSKU() {
        ProductSKUDTO input = new ProductSKUDTO();
        input.setSkuCode("SKU-NEW");
        input.setPrice(new BigDecimal("50.00"));

        ProductSKUDTO created = new ProductSKUDTO();
        created.setId(11L);
        created.setProductId(1L);
        created.setSkuCode("SKU-NEW");

        when(productService.createProductSKU(any(ProductSKUDTO.class))).thenReturn(created);

        ResponseEntity<ProductSKUDTO> response = productController.createProductSKU(1L, input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getProductId());
        assertEquals(11L, response.getBody().getId());
    }

    @Test
    void testUpdateProductSKU() {
        ProductSKUDTO input = new ProductSKUDTO();
        input.setPrice(new BigDecimal("75.00"));

        ProductSKUDTO updated = new ProductSKUDTO();
        updated.setId(10L);
        updated.setProductId(1L);
        updated.setPrice(new BigDecimal("75.00"));

        when(productService.updateProductSKU(eq(10L), any(ProductSKUDTO.class))).thenReturn(updated);

        ResponseEntity<ProductSKUDTO> response = productController.updateProductSKU(10L, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, new BigDecimal("75.00").compareTo(response.getBody().getPrice()));
    }

    @Test
    void testDeleteProductSKU() {
        doNothing().when(productService).deleteProductSKU(10L);

        ResponseEntity<Void> response = productController.deleteProductSKU(10L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(productService).deleteProductSKU(10L);
    }

    @Test
    void testDeleteProductNotFound() {
        doThrow(new ProductNotFoundException("Product not found with id: 999"))
                .when(productService).deleteProduct(999L);

        assertThrows(ProductNotFoundException.class,
                () -> productController.deleteProduct(999L));
    }
}
