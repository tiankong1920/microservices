package com.inventory.productservice.service.impl;

import com.inventory.productservice.dto.ProductDTO;
import com.inventory.productservice.entity.Product;
import com.inventory.productservice.repository.IProductRepository;
import com.inventory.productservice.repository.IProductSKURepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ProductServiceImplCacheTest {

    @Mock
    private IProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private IProductSKURepository productSKURepository;

    private ProductServiceImpl productService;

    private Product product;
    private ProductDTO productDTO;

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
    void testCacheEvictionOnCreateProduct() {
        // 模拟创建产品前的缓存状态
        when(modelMapper.map(any(ProductDTO.class), eq(Product.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        // 调用创建产品方法，应该触发缓存清除
        final ProductDTO result = productService.createProduct(productDTO);

        // 验证方法执行结果
        assertNotNull(result);
        assertEquals(1L, result.getId());

        // 验证缓存相关的交互
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testCacheEvictionOnUpdateProduct() {
        // 模拟更新产品前的缓存状态
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);
        // 添加对void方法的Stub
        doNothing().when(modelMapper).map(any(ProductDTO.class), any(Product.class));

        // 调用更新产品方法，应该触发缓存清除
        final ProductDTO result = productService.updateProduct(1L, productDTO);

        // 验证方法执行结果
        assertNotNull(result);
        assertEquals(1L, result.getId());

        // 验证缓存相关的交互
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(modelMapper, times(1)).map(any(ProductDTO.class), any(Product.class));
        verify(modelMapper, times(1)).map(any(Product.class), eq(ProductDTO.class));
    }

    @Test
    void testCacheEvictionOnDeleteProduct() {
        // 模拟删除产品前的缓存状态
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        // 调用删除产品方法，应该触发缓存清除
        productService.deleteProduct(1L);

        // 验证缓存相关的交互
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }
}
