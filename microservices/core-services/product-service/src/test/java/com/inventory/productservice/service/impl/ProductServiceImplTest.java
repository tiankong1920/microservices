package com.inventory.productservice.service.impl;

import com.inventory.productservice.dto.ProductDTO;
import com.inventory.productservice.entity.Product;
import com.inventory.productservice.exception.ProductNotFoundException;
import com.inventory.productservice.repository.IProductRepository;
import com.inventory.productservice.repository.IProductSKURepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ProductServiceImplTest {

    @Mock
    private IProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private IProductSKURepository productSKURepository;

    private ProductServiceImpl productService;

    private Product product;
    private ProductDTO productDTO;

    private static final Long NON_EXISTENT_PRODUCT_ID = 999L;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository, productSKURepository, modelMapper);
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setSku("TEST-001");
        product.setPrice(new BigDecimal(100.00));
        product.setStockQuantity(10);
        product.setStatus("ACTIVE");
        product.setIsActive(true);

        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setName("Test Product");
        productDTO.setDescription("Test Description");
        productDTO.setSku("TEST-001");
        productDTO.setPrice(new BigDecimal(100.00));
        productDTO.setStockQuantity(10);
        productDTO.setStatus("ACTIVE");
        productDTO.setIsActive(true);
    }

    @Test
    void testGetAllProducts() {
        final List<Product> productList = new ArrayList<>();
        productList.add(product);

        when(productRepository.findAll()).thenReturn(productList);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        final List<ProductDTO> result = productService.getAllProducts();

        assertEquals(1, result.size());
        verify(productRepository, times(1)).findAll();
        verify(modelMapper, times(1)).map(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void testGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        final ProductDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(productRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void testGetProductByIdNotFound() {
        when(productRepository.findById(NON_EXISTENT_PRODUCT_ID)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(NON_EXISTENT_PRODUCT_ID));
        verify(productRepository, times(1)).findById(NON_EXISTENT_PRODUCT_ID);
        verify(modelMapper, never()).map(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void testGetProductBySku() {
        when(productRepository.findBySku("TEST-001")).thenReturn(Optional.of(product));
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        final ProductDTO result = productService.getProductBySku("TEST-001");

        assertNotNull(result);
        assertEquals("TEST-001", result.getSku());
        verify(productRepository, times(1)).findBySku("TEST-001");
        verify(modelMapper, times(1)).map(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void testGetProductBySkuNotFound() {
        when(productRepository.findBySku("NON-EXISTENT")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductBySku("NON-EXISTENT"));
        verify(productRepository, times(1)).findBySku("NON-EXISTENT");
        verify(modelMapper, never()).map(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void testCreateProduct() {
        when(modelMapper.map(any(ProductDTO.class), eq(Product.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        final ProductDTO result = productService.createProduct(productDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(modelMapper, times(1)).map(any(ProductDTO.class), eq(Product.class));
        verify(modelMapper, times(1)).map(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void testUpdateProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);
        // 添加对void方法的Stub
        doNothing().when(modelMapper).map(any(ProductDTO.class), any(Product.class));

        final ProductDTO result = productService.updateProduct(1L, productDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(modelMapper, times(1)).map(any(ProductDTO.class), any(Product.class));
        verify(modelMapper, times(1)).map(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void testUpdateProductNotFound() {
        when(productRepository.findById(NON_EXISTENT_PRODUCT_ID)).thenReturn(Optional.empty());

        assertThrows(
            ProductNotFoundException.class,
            () -> productService.updateProduct(NON_EXISTENT_PRODUCT_ID, productDTO)
        );
        verify(productRepository, times(1)).findById(NON_EXISTENT_PRODUCT_ID);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteProductNotFound() {
        when(productRepository.existsById(NON_EXISTENT_PRODUCT_ID)).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(NON_EXISTENT_PRODUCT_ID));
        verify(productRepository, times(1)).existsById(NON_EXISTENT_PRODUCT_ID);
        verify(productRepository, never()).deleteById(anyLong());
    }
}
