package com.invoice.invoiceservice.service;

import java.util.List;

import com.invoice.invoiceservice.dto.CustomerInfoDTO;
import com.invoice.invoiceservice.dto.SearchResultDTO;

public interface PinyinSearchService {

    List<SearchResultDTO<CustomerInfoDTO>> searchByInitials(String initials);

    List<SearchResultDTO<CustomerInfoDTO>> searchByFullPinyin(String pinyin);

    List<SearchResultDTO<CustomerInfoDTO>> searchByMixedPinyin(String input);

    void rebuildPinyinIndex();
}
