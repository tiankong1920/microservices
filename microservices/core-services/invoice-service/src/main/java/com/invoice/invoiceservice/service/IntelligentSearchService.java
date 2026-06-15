package com.invoice.invoiceservice.service;

import java.util.List;

import com.invoice.invoiceservice.dto.CustomerInfoDTO;
import com.invoice.invoiceservice.dto.SearchResultDTO;

public interface IntelligentSearchService {

    List<SearchResultDTO<CustomerInfoDTO>> fuzzySearchCustomers(String keyword, double threshold);

    List<SearchResultDTO<CustomerInfoDTO>> searchByPinyin(String pinyin);

    List<SearchResultDTO<CustomerInfoDTO>> searchByPinyinInitials(String initials);

    List<SearchResultDTO<CustomerInfoDTO>> compositeSearch(String keyword, String pinyin, double threshold);

    List<CustomerInfoDTO> getFrequentlyUsedCustomers(int limit);

    List<CustomerInfoDTO> getRecentlyUsedCustomers(int limit);
}
