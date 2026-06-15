package com.invoice.invoiceservice.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.invoice.invoiceservice.dto.CustomerInfoDTO;
import com.invoice.invoiceservice.dto.SearchResultDTO;
import com.invoice.invoiceservice.entity.CustomerInfo;
import com.invoice.invoiceservice.entity.PinyinIndex;
import com.invoice.invoiceservice.repository.ICustomerInfoRepository;
import com.invoice.invoiceservice.repository.IPinyinIndexRepository;
import com.invoice.invoiceservice.service.PinyinSearchService;
import com.invoice.invoiceservice.util.AesEncryptionUtil;
import com.invoice.invoiceservice.util.PinyinUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PinyinSearchServiceImpl implements PinyinSearchService {

    private final IPinyinIndexRepository pinyinIndexRepository;
    private final ICustomerInfoRepository customerInfoRepository;
    private final AesEncryptionUtil aesEncryptionUtil;
    private final PinyinUtil pinyinUtil;

    @Override
    public List<SearchResultDTO<CustomerInfoDTO>> searchByInitials(String initials) {
        List<PinyinIndex> indices = pinyinIndexRepository.findByPinyinInitialsStartingWith(initials);

        List<Long> customerIds = indices.stream()
                .filter(pi -> "CUSTOMER_INFO".equals(pi.getEntityType()))
                .map(PinyinIndex::getEntityId)
                .toList();

        if (customerIds.isEmpty()) {
            return List.of();
        }

        Map<Long, CustomerInfo> customerMap = customerInfoRepository
                .findByIdInAndDeletedFalseAndStatus(customerIds, CustomerInfo.CustomerStatus.ENABLED)
                .stream()
                .collect(Collectors.toMap(CustomerInfo::getId, c -> c));

        return indices.stream()
                .filter(pi -> "CUSTOMER_INFO".equals(pi.getEntityType()))
                .map(pi -> customerMap.get(pi.getEntityId()))
                .filter(java.util.Objects::nonNull)
                .map(customer -> {
                    CustomerInfoDTO dto = mapToDTO(customer);
                    double score = calculateInitialsScore(initials, customer.getPinyinInitials());
                    return SearchResultDTO.of(dto, score, "PINYIN_INITIALS");
                })
                .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SearchResultDTO<CustomerInfoDTO>> searchByFullPinyin(String pinyin) {
        List<PinyinIndex> indices = pinyinIndexRepository.searchByPinyin(pinyin, pinyin);

        List<Long> customerIds = indices.stream()
                .filter(pi -> "CUSTOMER_INFO".equals(pi.getEntityType()))
                .map(PinyinIndex::getEntityId)
                .toList();

        if (customerIds.isEmpty()) {
            return List.of();
        }

        Map<Long, CustomerInfo> customerMap = customerInfoRepository
                .findByIdInAndDeletedFalseAndStatus(customerIds, CustomerInfo.CustomerStatus.ENABLED)
                .stream()
                .collect(Collectors.toMap(CustomerInfo::getId, c -> c));

        return indices.stream()
                .filter(pi -> "CUSTOMER_INFO".equals(pi.getEntityType()))
                .map(pi -> customerMap.get(pi.getEntityId()))
                .filter(java.util.Objects::nonNull)
                .map(customer -> {
                    CustomerInfoDTO dto = mapToDTO(customer);
                    return SearchResultDTO.of(dto, 1.0, "PINYIN_FULL");
                })
                .sorted((a, b) -> Integer.compare(
                        b.getData().getUsageCount() != null ? b.getData().getUsageCount() : 0,
                        a.getData().getUsageCount() != null ? a.getData().getUsageCount() : 0))
                .collect(Collectors.toList());
    }

    @Override
    public List<SearchResultDTO<CustomerInfoDTO>> searchByMixedPinyin(String input) {
        List<SearchResultDTO<CustomerInfoDTO>> results = searchByInitials(input);

        List<SearchResultDTO<CustomerInfoDTO>> fullResults = searchByFullPinyin(input);
        for (SearchResultDTO<CustomerInfoDTO> fr : fullResults) {
            boolean exists = results.stream()
                    .anyMatch(r -> r.getData().getId().equals(fr.getData().getId()));
            if (!exists) {
                results.add(fr);
            }
        }

        return results.stream()
                .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
                .collect(Collectors.toList());
    }

    @Override
    @Scheduled(cron = "${invoice.search.pinyin-index-cron}")
    @Transactional
    public void rebuildPinyinIndex() {
        log.info("开始重建拼音索引...");
        long startTime = System.currentTimeMillis();

        List<CustomerInfo> customers = customerInfoRepository.findByDeletedFalse();
        int count = 0;

        for (CustomerInfo customer : customers) {
            String initials = pinyinUtil.getInitials(customer.getCustomerName());
            String fullPinyin = pinyinUtil.getFullPinyin(customer.getCustomerName());

            customer.setPinyinInitials(initials);
            customer.setPinyinFull(fullPinyin);
            customerInfoRepository.save(customer);

            pinyinIndexRepository.deleteByEntityTypeAndEntityId("CUSTOMER_INFO", customer.getId());

            PinyinIndex index = new PinyinIndex();
            index.setEntityType("CUSTOMER_INFO");
            index.setEntityId(customer.getId());
            index.setEntityName(customer.getCustomerName());
            index.setPinyinInitials(initials);
            index.setPinyinFull(fullPinyin);
            index.setTenantId(customer.getTenantId());
            pinyinIndexRepository.save(index);

            count++;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("拼音索引重建完成，处理{}条记录，耗时{}ms", count, elapsed);
    }

    private double calculateInitialsScore(String input, String target) {
        if (target == null || target.isEmpty()) return 0.0;
        if (target.equals(input)) return 1.0;
        if (target.startsWith(input)) return 0.9;
        return 0.5 * (1.0 - (double) Math.abs(input.length() - target.length()) / Math.max(input.length(), target.length()));
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
