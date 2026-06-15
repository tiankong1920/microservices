package com.invoice.invoiceservice.service.impl;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.invoice.invoiceservice.dto.CustomerInfoDTO;
import com.invoice.invoiceservice.entity.CustomerInfo;
import com.invoice.invoiceservice.entity.OperationLog;
import com.invoice.invoiceservice.entity.PinyinIndex;
import com.invoice.invoiceservice.exception.CustomerInfoNotFoundException;
import com.invoice.invoiceservice.exception.DuplicateTaxNumberException;
import com.invoice.invoiceservice.repository.ICustomerInfoRepository;
import com.invoice.invoiceservice.repository.IOperationLogRepository;
import com.invoice.invoiceservice.repository.IPinyinIndexRepository;
import com.invoice.invoiceservice.service.CustomerInfoService;
import com.invoice.invoiceservice.util.AesEncryptionUtil;
import com.invoice.invoiceservice.util.PinyinUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerInfoServiceImpl implements CustomerInfoService {

    private final ICustomerInfoRepository customerInfoRepository;
    private final IOperationLogRepository operationLogRepository;
    private final IPinyinIndexRepository pinyinIndexRepository;
    private final AesEncryptionUtil aesEncryptionUtil;
    private final PinyinUtil pinyinUtil;

    @Override
    public CustomerInfoDTO createCustomer(CustomerInfoDTO dto) {
        if (customerInfoRepository.existsByTaxNumberAndDeletedFalse(dto.getTaxNumber())) {
            throw new DuplicateTaxNumberException(dto.getTaxNumber());
        }

        CustomerInfo entity = mapToEntity(dto);
        entity.setTaxNumber(aesEncryptionUtil.encrypt(dto.getTaxNumber()));
        entity.setBankAccount(aesEncryptionUtil.encrypt(dto.getBankAccount()));

        String initials = pinyinUtil.getInitials(dto.getCustomerName());
        String fullPinyin = pinyinUtil.getFullPinyin(dto.getCustomerName());
        entity.setPinyinInitials(initials);
        entity.setPinyinFull(fullPinyin);

        CustomerInfo saved = customerInfoRepository.save(entity);

        savePinyinIndex(saved.getId(), dto.getCustomerName(), initials, fullPinyin, dto.getTenantId());
        logOperation(OperationLog.EntityType.CUSTOMER_INFO, saved.getId(),
                OperationLog.OperationType.CREATE, null, dto.toString(), dto.getCreatedBy());

        return mapToDTO(saved);
    }

    @Override
    @CachePut(value = "customerInfo", key = "#id")
    public CustomerInfoDTO updateCustomer(Long id, CustomerInfoDTO dto) {
        CustomerInfo entity = findEntityOrThrow(id);

        if (dto.getTaxNumber() != null &&
                customerInfoRepository.existsByTaxNumberAndDeletedFalseAndIdNot(dto.getTaxNumber(), id)) {
            throw new DuplicateTaxNumberException(dto.getTaxNumber());
        }

        String oldValue = entity.toString();

        if (dto.getCustomerName() != null) {
            entity.setCustomerName(dto.getCustomerName());
            String initials = pinyinUtil.getInitials(dto.getCustomerName());
            String fullPinyin = pinyinUtil.getFullPinyin(dto.getCustomerName());
            entity.setPinyinInitials(initials);
            entity.setPinyinFull(fullPinyin);
            updatePinyinIndex(id, dto.getCustomerName(), initials, fullPinyin, entity.getTenantId());
        }
        if (dto.getTaxNumber() != null) {
            entity.setTaxNumber(aesEncryptionUtil.encrypt(dto.getTaxNumber()));
        }
        if (dto.getRegisteredAddress() != null) {
            entity.setRegisteredAddress(dto.getRegisteredAddress());
        }
        if (dto.getContactPhone() != null) {
            entity.setContactPhone(dto.getContactPhone());
        }
        if (dto.getMobilePhone() != null) {
            entity.setMobilePhone(dto.getMobilePhone());
        }
        if (dto.getBankName() != null) {
            entity.setBankName(dto.getBankName());
        }
        if (dto.getBankAccount() != null) {
            entity.setBankAccount(aesEncryptionUtil.encrypt(dto.getBankAccount()));
        }
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.getContactPerson() != null) {
            entity.setContactPerson(dto.getContactPerson());
        }
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }
        if (dto.getUpdatedBy() != null) {
            entity.setUpdatedBy(dto.getUpdatedBy());
        }

        CustomerInfo updated = customerInfoRepository.save(entity);
        logOperation(OperationLog.EntityType.CUSTOMER_INFO, id,
                OperationLog.OperationType.UPDATE, oldValue, updated.toString(), dto.getUpdatedBy());

        return mapToDTO(updated);
    }

    @Override
    @Cacheable(value = "customerInfo", key = "#id")
    @Transactional(readOnly = true)
    public CustomerInfoDTO getCustomerById(Long id) {
        return mapToDTO(findEntityOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerInfoDTO> getAllCustomers() {
        return customerInfoRepository.findByDeletedFalse().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerInfoDTO> getActiveCustomers() {
        return customerInfoRepository.findByDeletedFalseAndStatus(CustomerInfo.CustomerStatus.ENABLED).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @CacheEvict(value = "customerInfo", key = "#id")
    public void deleteCustomer(Long id) {
        CustomerInfo entity = findEntityOrThrow(id);
        entity.setDeleted(true);
        customerInfoRepository.save(entity);
        logOperation(OperationLog.EntityType.CUSTOMER_INFO, id,
                OperationLog.OperationType.DELETE, entity.toString(), null, null);
    }

    @Override
    @CacheEvict(value = "customerInfo", key = "#id")
    public void enableCustomer(Long id) {
        CustomerInfo entity = findEntityOrThrow(id);
        entity.setStatus(CustomerInfo.CustomerStatus.ENABLED);
        customerInfoRepository.save(entity);
        logOperation(OperationLog.EntityType.CUSTOMER_INFO, id,
                OperationLog.OperationType.ENABLE, "DISABLED", "ENABLED", null);
    }

    @Override
    @CacheEvict(value = "customerInfo", key = "#id")
    public void disableCustomer(Long id) {
        CustomerInfo entity = findEntityOrThrow(id);
        entity.setStatus(CustomerInfo.CustomerStatus.DISABLED);
        customerInfoRepository.save(entity);
        logOperation(OperationLog.EntityType.CUSTOMER_INFO, id,
                OperationLog.OperationType.DISABLE, "ENABLED", "DISABLED", null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerInfoDTO> searchCustomers(String keyword) {
        List<CustomerInfoDTO> results = customerInfoRepository.searchByKeywordOrderByUsage(keyword).stream()
                .map(this::mapToDTO)
                .toList();
        logOperation(OperationLog.EntityType.CUSTOMER_INFO, null,
                OperationLog.OperationType.SEARCH, null, "keyword=" + keyword, null);
        return results;
    }

    @Override
    public void incrementUsageCount(Long id) {
        CustomerInfo entity = findEntityOrThrow(id);
        entity.setUsageCount(entity.getUsageCount() + 1);
        entity.setLastUsedAt(java.time.LocalDateTime.now());
        customerInfoRepository.save(entity);
    }

    @Override
    @Cacheable(value = "customerInfo", key = "#id")
    @Transactional(readOnly = true)
    public CustomerInfoDTO autoFillCustomerInfo(Long id) {
        CustomerInfoDTO dto = mapToDTO(findEntityOrThrow(id));
        incrementUsageCount(id);
        logOperation(OperationLog.EntityType.CUSTOMER_INFO, id,
                OperationLog.OperationType.AUTO_FILL, null, "auto-filled", null);
        return dto;
    }

    private CustomerInfo findEntityOrThrow(Long id) {
        return customerInfoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomerInfoNotFoundException(id));
    }

    private void savePinyinIndex(Long entityId, String name, String initials, String fullPinyin, String tenantId) {
        PinyinIndex pinyinIndex = new PinyinIndex();
        pinyinIndex.setEntityType("CUSTOMER_INFO");
        pinyinIndex.setEntityId(entityId);
        pinyinIndex.setEntityName(name);
        pinyinIndex.setPinyinInitials(initials);
        pinyinIndex.setPinyinFull(fullPinyin);
        pinyinIndex.setTenantId(tenantId);
        pinyinIndexRepository.save(pinyinIndex);
    }

    private void updatePinyinIndex(Long entityId, String name, String initials, String fullPinyin, String tenantId) {
        pinyinIndexRepository.deleteByEntityTypeAndEntityId("CUSTOMER_INFO", entityId);
        savePinyinIndex(entityId, name, initials, fullPinyin, tenantId);
    }

    private void logOperation(OperationLog.EntityType entityType, Long entityId,
                              OperationLog.OperationType opType, String oldValue, String newValue, String operator) {
        OperationLog opLog = new OperationLog();
        opLog.setEntityType(entityType);
        opLog.setEntityId(entityId != null ? entityId : 0L);
        opLog.setOperationType(opType);
        opLog.setOldValue(oldValue);
        opLog.setNewValue(newValue);
        opLog.setOperator(operator);
        operationLogRepository.save(opLog);
    }

    private CustomerInfo mapToEntity(CustomerInfoDTO dto) {
        CustomerInfo entity = new CustomerInfo();
        entity.setCustomerName(dto.getCustomerName());
        entity.setTaxNumber(aesEncryptionUtil.encrypt(dto.getTaxNumber()));
        entity.setRegisteredAddress(dto.getRegisteredAddress());
        entity.setContactPhone(dto.getContactPhone());
        entity.setMobilePhone(dto.getMobilePhone());
        entity.setBankName(dto.getBankName());
        entity.setBankAccount(aesEncryptionUtil.encrypt(dto.getBankAccount()));
        entity.setEmail(dto.getEmail());
        entity.setContactPerson(dto.getContactPerson());
        entity.setRemark(dto.getRemark());
        entity.setTenantId(dto.getTenantId());
        entity.setCreatedBy(dto.getCreatedBy());
        return entity;
    }

    private CustomerInfoDTO mapToDTO(CustomerInfo entity) {
        return CustomerInfoDTO.builder()
                .id(entity.getId())
                .customerName(entity.getCustomerName())
                .taxNumber(aesEncryptionUtil.decrypt(entity.getTaxNumber()))
                .registeredAddress(entity.getRegisteredAddress())
                .contactPhone(entity.getContactPhone())
                .mobilePhone(entity.getMobilePhone())
                .bankName(entity.getBankName())
                .bankAccount(aesEncryptionUtil.decrypt(entity.getBankAccount()))
                .email(entity.getEmail())
                .contactPerson(entity.getContactPerson())
                .remark(entity.getRemark())
                .pinyinInitials(entity.getPinyinInitials())
                .pinyinFull(entity.getPinyinFull())
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
