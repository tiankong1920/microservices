package com.invoice.invoiceservice.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.invoice.invoiceservice.dto.CustomerInfoDTO;
import com.invoice.invoiceservice.dto.SearchResultDTO;
import com.invoice.invoiceservice.entity.CustomerInfo;
import com.invoice.invoiceservice.entity.PinyinIndex;
import com.invoice.invoiceservice.repository.ICustomerInfoRepository;
import com.invoice.invoiceservice.repository.IPinyinIndexRepository;
import com.invoice.invoiceservice.service.IntelligentSearchService;
import com.invoice.invoiceservice.util.AesEncryptionUtil;
import com.invoice.invoiceservice.util.LevenshteinDistanceUtil;
import com.invoice.invoiceservice.util.PinyinUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class IntelligentSearchServiceImpl implements IntelligentSearchService {

    private final ICustomerInfoRepository customerInfoRepository;
    private final IPinyinIndexRepository pinyinIndexRepository;
    private final AesEncryptionUtil aesEncryptionUtil;
    private final PinyinUtil pinyinUtil;

    @Override
    @Cacheable(value = "customerInfo", key = "'fuzzy_' + #keyword + '_' + #threshold")
    public List<SearchResultDTO<CustomerInfoDTO>> fuzzySearchCustomers(String keyword, double threshold) {
        List<CustomerInfo> allActive = customerInfoRepository.findByDeletedFalseAndStatus(CustomerInfo.CustomerStatus.ENABLED);

        List<SearchResultDTO<CustomerInfoDTO>> results = new ArrayList<>();

        for (CustomerInfo customer : allActive) {
            double nameSimilarity = LevenshteinDistanceUtil.similarity(
                    keyword.toLowerCase(), customer.getCustomerName().toLowerCase());
            double addressSimilarity = 0.0;
            if (customer.getRegisteredAddress() != null) {
                addressSimilarity = LevenshteinDistanceUtil.similarity(
                        keyword.toLowerCase(), customer.getRegisteredAddress().toLowerCase());
            }
            double maxSimilarity = Math.max(nameSimilarity, addressSimilarity);

            if (maxSimilarity >= threshold) {
                CustomerInfoDTO dto = mapToDTO(customer);
                results.add(SearchResultDTO.of(dto, maxSimilarity, "FUZZY"));
            }
        }

        return results.stream()
                .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "customerInfo", key = "'pinyin_' + #pinyin")
    public List<SearchResultDTO<CustomerInfoDTO>> searchByPinyin(String pinyin) {
        List<PinyinIndex> pinyinIndices = pinyinIndexRepository.searchByPinyin(pinyin, pinyin);

        List<Long> customerIds = pinyinIndices.stream()
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

        return pinyinIndices.stream()
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
    @Cacheable(value = "customerInfo", key = "'pinyin_init_' + #initials")
    public List<SearchResultDTO<CustomerInfoDTO>> searchByPinyinInitials(String initials) {
        List<PinyinIndex> pinyinIndices = pinyinIndexRepository.findByPinyinInitialsStartingWith(initials);

        List<Long> customerIds = pinyinIndices.stream()
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

        return pinyinIndices.stream()
                .filter(pi -> "CUSTOMER_INFO".equals(pi.getEntityType()))
                .map(pi -> customerMap.get(pi.getEntityId()))
                .filter(java.util.Objects::nonNull)
                .map(customer -> {
                    CustomerInfoDTO dto = mapToDTO(customer);
                    double score = calculateInitialsMatchScore(initials, customer.getPinyinInitials());
                    return SearchResultDTO.of(dto, score, "PINYIN_INITIALS");
                })
                .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SearchResultDTO<CustomerInfoDTO>> compositeSearch(String keyword, String pinyin, double threshold) {
        List<SearchResultDTO<CustomerInfoDTO>> results = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            List<CustomerInfo> exactMatches = customerInfoRepository.searchByKeywordOrderByUsage(keyword);
            for (CustomerInfo c : exactMatches) {
                results.add(SearchResultDTO.of(mapToDTO(c), 1.0, "EXACT"));
            }

            List<SearchResultDTO<CustomerInfoDTO>> fuzzyResults = fuzzySearchCustomers(keyword, threshold);
            for (SearchResultDTO<CustomerInfoDTO> fr : fuzzyResults) {
                boolean alreadyExists = results.stream()
                        .anyMatch(r -> r.getData().getId().equals(fr.getData().getId()));
                if (!alreadyExists) {
                    results.add(fr);
                }
            }
        }

        if (pinyin != null && !pinyin.isEmpty()) {
            List<SearchResultDTO<CustomerInfoDTO>> pinyinResults = searchByPinyin(pinyin);
            for (SearchResultDTO<CustomerInfoDTO> pr : pinyinResults) {
                boolean alreadyExists = results.stream()
                        .anyMatch(r -> r.getData().getId().equals(pr.getData().getId()));
                if (!alreadyExists) {
                    results.add(pr);
                }
            }
        }

        return results.stream()
                .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerInfoDTO> getFrequentlyUsedCustomers(int limit) {
        return customerInfoRepository.findAllOrderByUsageDesc().stream()
                .limit(limit)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerInfoDTO> getRecentlyUsedCustomers(int limit) {
        return customerInfoRepository.findByDeletedFalseAndStatus(CustomerInfo.CustomerStatus.ENABLED).stream()
                .filter(c -> c.getLastUsedAt() != null)
                .sorted((a, b) -> b.getLastUsedAt().compareTo(a.getLastUsedAt()))
                .limit(limit)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private double calculateInitialsMatchScore(String input, String target) {
        if (target == null || target.isEmpty()) {
            return 0.0;
        }
        if (target.startsWith(input)) {
            return 1.0 - (input.length() - target.length()) * 0.05;
        }
        return LevenshteinDistanceUtil.similarity(input.toLowerCase(), target.toLowerCase());
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
