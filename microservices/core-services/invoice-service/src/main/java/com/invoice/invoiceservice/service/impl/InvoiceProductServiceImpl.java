package com.invoice.invoiceservice.service.impl;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.invoice.invoiceservice.dto.InvoiceProductDTO;
import com.invoice.invoiceservice.entity.InvoiceProduct;
import com.invoice.invoiceservice.entity.OperationLog;
import com.invoice.invoiceservice.entity.PinyinIndex;
import com.invoice.invoiceservice.exception.ProductNotFoundException;
import com.invoice.invoiceservice.repository.IInvoiceProductRepository;
import com.invoice.invoiceservice.repository.IOperationLogRepository;
import com.invoice.invoiceservice.repository.IPinyinIndexRepository;
import com.invoice.invoiceservice.service.InvoiceProductService;
import com.invoice.invoiceservice.util.PinyinUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceProductServiceImpl implements InvoiceProductService {

    private final IInvoiceProductRepository productRepository;
    private final IOperationLogRepository operationLogRepository;
    private final IPinyinIndexRepository pinyinIndexRepository;
    private final PinyinUtil pinyinUtil;

    @Override
    public InvoiceProductDTO createProduct(InvoiceProductDTO dto) {
        InvoiceProduct entity = mapToEntity(dto);

        String initials = pinyinUtil.getInitials(dto.getProductName());
        String fullPinyin = pinyinUtil.getFullPinyin(dto.getProductName());

        InvoiceProduct saved = productRepository.save(entity);

        PinyinIndex pinyinIndex = new PinyinIndex();
        pinyinIndex.setEntityType("INVOICE_PRODUCT");
        pinyinIndex.setEntityId(saved.getId());
        pinyinIndex.setEntityName(dto.getProductName());
        pinyinIndex.setPinyinInitials(initials);
        pinyinIndex.setPinyinFull(fullPinyin);
        pinyinIndex.setTenantId(dto.getTenantId());
        pinyinIndexRepository.save(pinyinIndex);

        logOperation(OperationLog.EntityType.INVOICE_PRODUCT, saved.getId(),
                OperationLog.OperationType.CREATE, null, dto.toString(), dto.getCreatedBy());

        return mapToDTO(saved);
    }

    @Override
    @CachePut(value = "invoiceProduct", key = "#id")
    public InvoiceProductDTO updateProduct(Long id, InvoiceProductDTO dto) {
        InvoiceProduct entity = findEntityOrThrow(id);

        String oldValue = entity.toString();

        if (dto.getProductName() != null) entity.setProductName(dto.getProductName());
        if (dto.getSpecification() != null) entity.setSpecification(dto.getSpecification());
        if (dto.getUnitName() != null) entity.setUnitName(dto.getUnitName());
        if (dto.getUnitPrice() != null) entity.setUnitPrice(dto.getUnitPrice());
        if (dto.getTaxRate() != null) entity.setTaxRate(dto.getTaxRate());
        if (dto.getProductCategory() != null) entity.setProductCategory(dto.getProductCategory());
        if (dto.getTaxCategoryCode() != null) entity.setTaxCategoryCode(dto.getTaxCategoryCode());
        if (dto.getUpdatedBy() != null) entity.setUpdatedBy(dto.getUpdatedBy());

        InvoiceProduct updated = productRepository.save(entity);
        logOperation(OperationLog.EntityType.INVOICE_PRODUCT, id,
                OperationLog.OperationType.UPDATE, oldValue, updated.toString(), dto.getUpdatedBy());

        return mapToDTO(updated);
    }

    @Override
    @Cacheable(value = "invoiceProduct", key = "#id")
    @Transactional(readOnly = true)
    public InvoiceProductDTO getProductById(Long id) {
        return mapToDTO(findEntityOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceProductDTO> getAllProducts() {
        return productRepository.findByDeletedFalse().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceProductDTO> getActiveProducts() {
        return productRepository.findByDeletedFalseAndStatus(InvoiceProduct.ProductStatus.ENABLED).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @CacheEvict(value = "invoiceProduct", key = "#id")
    public void deleteProduct(Long id) {
        InvoiceProduct entity = findEntityOrThrow(id);
        entity.setDeleted(true);
        productRepository.save(entity);
        logOperation(OperationLog.EntityType.INVOICE_PRODUCT, id,
                OperationLog.OperationType.DELETE, entity.toString(), null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceProductDTO> searchProducts(String keyword) {
        return productRepository.searchByKeywordOrderByUsage(keyword).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public void incrementUsageCount(Long id) {
        InvoiceProduct entity = findEntityOrThrow(id);
        entity.setUsageCount(entity.getUsageCount() + 1);
        entity.setLastUsedAt(java.time.LocalDateTime.now());
        productRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceProductDTO> getFrequentlyUsedProducts(int limit) {
        return productRepository.findAllOrderByUsageDesc().stream()
                .limit(limit)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceProductDTO> recommendProductsByCustomer(Long customerId, int limit) {
        return productRepository.findAllOrderByUsageDesc().stream()
                .limit(limit)
                .map(this::mapToDTO)
                .toList();
    }

    private InvoiceProduct findEntityOrThrow(Long id) {
        return productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private void logOperation(OperationLog.EntityType entityType, Long entityId,
                              OperationLog.OperationType opType, String oldValue, String newValue, String operator) {
        OperationLog opLog = new OperationLog();
        opLog.setEntityType(entityType);
        opLog.setEntityId(entityId);
        opLog.setOperationType(opType);
        opLog.setOldValue(oldValue);
        opLog.setNewValue(newValue);
        opLog.setOperator(operator);
        operationLogRepository.save(opLog);
    }

    private InvoiceProduct mapToEntity(InvoiceProductDTO dto) {
        InvoiceProduct entity = new InvoiceProduct();
        entity.setProductName(dto.getProductName());
        entity.setSpecification(dto.getSpecification());
        entity.setUnitName(dto.getUnitName());
        entity.setUnitPrice(dto.getUnitPrice());
        entity.setTaxRate(dto.getTaxRate());
        entity.setProductCategory(dto.getProductCategory());
        entity.setTaxCategoryCode(dto.getTaxCategoryCode());
        entity.setTenantId(dto.getTenantId());
        entity.setCreatedBy(dto.getCreatedBy());
        return entity;
    }

    private InvoiceProductDTO mapToDTO(InvoiceProduct entity) {
        return InvoiceProductDTO.builder()
                .id(entity.getId())
                .productName(entity.getProductName())
                .specification(entity.getSpecification())
                .unitName(entity.getUnitName())
                .unitPrice(entity.getUnitPrice())
                .taxRate(entity.getTaxRate())
                .productCategory(entity.getProductCategory())
                .taxCategoryCode(entity.getTaxCategoryCode())
                .status(entity.getStatus().name())
                .usageCount(entity.getUsageCount())
                .lastUsedAt(entity.getLastUsedAt())
                .tenantId(entity.getTenantId())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
