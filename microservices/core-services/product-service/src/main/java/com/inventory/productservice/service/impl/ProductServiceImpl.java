package com.inventory.productservice.service.impl;

import java.util.List;

import com.inventory.productservice.dto.ProductDTO;
import com.inventory.productservice.dto.ProductSKUDTO;
import com.inventory.productservice.entity.Product;
import com.inventory.productservice.entity.ProductSKU;
import com.inventory.productservice.exception.ProductNotFoundException;
import com.inventory.productservice.repository.IProductRepository;
import com.inventory.productservice.repository.IProductSKURepository;
import com.inventory.productservice.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProductServiceImpl implements ProductService {

    private static final String CACHE_NAME = "products";

    private final IProductRepository productRepository;
    private final IProductSKURepository productSKURepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ProductDTO> getAllProducts() {
        log.info("Getting all products");
        final List<Product> products = productRepository.findAll();
        log.info("Found {} products", products.size());
        return products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();
    }

    @Override
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.info("Getting products with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(product -> modelMapper.map(product, ProductDTO.class));
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "customCacheKeyGenerator")
    public ProductDTO getProductById(Long id) {
        log.info("Getting product by id: {}", id);
        final Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "customCacheKeyGenerator")
    public ProductDTO getProductBySku(String sku) {
        log.info("Getting product by sku: {}", sku);
        final Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with sku: " + sku));
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating product: {}", productDTO.getName());
        final Product product = modelMapper.map(productDTO, Product.class);
        final Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    @CachePut(value = CACHE_NAME, keyGenerator = "customCacheKeyGenerator")
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with id: {}", id);
        final Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        modelMapper.map(productDTO, existingProduct);
        final Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully with id: {}", updatedProduct.getId());
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted successfully with id: {}", id);
    }

    @Override
    public List<ProductSKUDTO> getProductSKUs(final Long productId) {
        log.info("Getting SKUs for product id: {}", productId);
        List<ProductSKU> skus = productSKURepository.findAllByProductId(productId);
        return skus.stream()
                .map(sku -> modelMapper.map(sku, ProductSKUDTO.class))
                .toList();
    }

    @Override
    public ProductSKUDTO getProductSKUByCode(final String skuCode) {
        log.info("Getting product SKU by code: {}", skuCode);
        ProductSKU sku = productSKURepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new ProductNotFoundException("Product SKU not found with code: " + skuCode));
        return modelMapper.map(sku, ProductSKUDTO.class);
    }

    @Override
    @Transactional
    public ProductSKUDTO createProductSKU(final ProductSKUDTO skuDTO) {
        log.info("Creating product SKU: {}", skuDTO.getSkuCode());
        Product product = productRepository.findById(skuDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + skuDTO.getProductId()));
        ProductSKU sku = modelMapper.map(skuDTO, ProductSKU.class);
        sku.setProduct(product);
        ProductSKU savedSKU = productSKURepository.save(sku);
        log.info("Product SKU created successfully with id: {}", savedSKU.getId());
        return modelMapper.map(savedSKU, ProductSKUDTO.class);
    }

    @Override
    @Transactional
    public ProductSKUDTO updateProductSKU(final Long skuId, final ProductSKUDTO skuDTO) {
        log.info("Updating product SKU with id: {}", skuId);
        ProductSKU existingSKU = productSKURepository.findById(skuId)
                .orElseThrow(() -> new ProductNotFoundException("Product SKU not found with id: " + skuId));
        existingSKU.setSkuCode(skuDTO.getSkuCode());
        existingSKU.setAttributes(skuDTO.getAttributes());
        existingSKU.setPrice(skuDTO.getPrice());
        existingSKU.setIsActive(skuDTO.getIsActive());
        ProductSKU updatedSKU = productSKURepository.save(existingSKU);
        log.info("Product SKU updated successfully with id: {}", updatedSKU.getId());
        return modelMapper.map(updatedSKU, ProductSKUDTO.class);
    }

    @Override
    @Transactional
    public void deleteProductSKU(final Long skuId) {
        log.info("Deleting product SKU with id: {}", skuId);
        if (!productSKURepository.existsById(skuId)) {
            throw new ProductNotFoundException("Product SKU not found with id: " + skuId);
        }
        productSKURepository.deleteById(skuId);
        log.info("Product SKU deleted successfully with id: {}", skuId);
    }
}
