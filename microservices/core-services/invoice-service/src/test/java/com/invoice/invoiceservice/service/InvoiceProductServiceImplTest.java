package com.invoice.invoiceservice.service;

import com.invoice.invoiceservice.dto.InvoiceProductDTO;
import com.invoice.invoiceservice.entity.InvoiceProduct;
import com.invoice.invoiceservice.entity.InvoiceProduct.ProductStatus;
import com.invoice.invoiceservice.entity.OperationLog;
import com.invoice.invoiceservice.entity.PinyinIndex;
import com.invoice.invoiceservice.exception.ProductNotFoundException;
import com.invoice.invoiceservice.repository.IInvoiceProductRepository;
import com.invoice.invoiceservice.repository.IOperationLogRepository;
import com.invoice.invoiceservice.repository.IPinyinIndexRepository;
import com.invoice.invoiceservice.service.impl.InvoiceProductServiceImpl;
import com.invoice.invoiceservice.util.PinyinUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceProductServiceImplTest {

    @Mock
    private IInvoiceProductRepository productRepository;

    @Mock
    private IOperationLogRepository operationLogRepository;

    @Mock
    private IPinyinIndexRepository pinyinIndexRepository;

    @Mock
    private PinyinUtil pinyinUtil;

    private InvoiceProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new InvoiceProductServiceImpl(
                productRepository, operationLogRepository, pinyinIndexRepository, pinyinUtil);
    }

    @Test
    void createProduct_shouldCreateSuccessfully() {
        InvoiceProductDTO dto = InvoiceProductDTO.builder()
                .productName("Test Product")
                .unitName("个")
                .unitPrice(BigDecimal.valueOf(100))
                .taxRate(BigDecimal.valueOf(0.13))
                .tenantId("tenant-001")
                .createdBy("admin")
                .build();

        when(pinyinUtil.getInitials("Test Product")).thenReturn("TCP");
        when(pinyinUtil.getFullPinyin("Test Product")).thenReturn("test chang pin");
        when(productRepository.save(any(InvoiceProduct.class))).thenAnswer(invocation -> {
            InvoiceProduct entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });
        when(pinyinIndexRepository.save(any(PinyinIndex.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(operationLogRepository.save(any(OperationLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceProductDTO result = productService.createProduct(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProductName()).isEqualTo("Test Product");
        verify(productRepository).save(any(InvoiceProduct.class));
        verify(pinyinIndexRepository).save(any(PinyinIndex.class));
    }

    @Test
    void getProductById_shouldReturnProduct() {
        InvoiceProduct entity = new InvoiceProduct();
        entity.setId(1L);
        entity.setProductName("Test Product");
        entity.setDeleted(false);

        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(entity));

        InvoiceProductDTO result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProductName()).isEqualTo("Test Product");
    }

    @Test
    void getProductById_shouldThrowExceptionWhenNotFound() {
        when(productRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getAllProducts_shouldReturnList() {
        InvoiceProduct entity1 = new InvoiceProduct();
        entity1.setId(1L);
        entity1.setProductName("Product 1");
        entity1.setDeleted(false);

        InvoiceProduct entity2 = new InvoiceProduct();
        entity2.setId(2L);
        entity2.setProductName("Product 2");
        entity2.setDeleted(false);

        when(productRepository.findByDeletedFalse()).thenReturn(List.of(entity1, entity2));

        List<InvoiceProductDTO> result = productService.getAllProducts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProductName()).isEqualTo("Product 1");
        assertThat(result.get(1).getProductName()).isEqualTo("Product 2");
    }

    @Test
    void getActiveProducts_shouldReturnActiveOnly() {
        InvoiceProduct entity = new InvoiceProduct();
        entity.setId(1L);
        entity.setProductName("Active Product");
        entity.setStatus(ProductStatus.ENABLED);
        entity.setDeleted(false);

        when(productRepository.findByDeletedFalseAndStatus(ProductStatus.ENABLED)).thenReturn(List.of(entity));

        List<InvoiceProductDTO> result = productService.getActiveProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).isEqualTo("Active Product");
    }

    @Test
    void deleteProduct_shouldSoftDelete() {
        InvoiceProduct entity = new InvoiceProduct();
        entity.setId(1L);
        entity.setProductName("To Delete");
        entity.setDeleted(false);

        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(entity));
        when(operationLogRepository.save(any(OperationLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.deleteProduct(1L);

        verify(productRepository).save(any(InvoiceProduct.class));
        verify(operationLogRepository).save(any(OperationLog.class));
    }
}
