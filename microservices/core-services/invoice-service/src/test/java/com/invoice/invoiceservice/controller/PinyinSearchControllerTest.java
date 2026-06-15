package com.invoice.invoiceservice.controller;

import com.invoice.invoiceservice.dto.CustomerInfoDTO;
import com.invoice.invoiceservice.dto.SearchResultDTO;
import com.invoice.invoiceservice.service.PinyinSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class PinyinSearchControllerTest {

    @Mock
    private PinyinSearchService pinyinSearchService;

    @InjectMocks
    private PinyinSearchController pinyinSearchController;

    @Test
    void testSearchByInitials() {
        CustomerInfoDTO customer = new CustomerInfoDTO();
        customer.setId(1L);
        customer.setCustomerName("Acme");
        SearchResultDTO<CustomerInfoDTO> result = new SearchResultDTO<>();
        result.setMatchType("initials");
        result.setData(customer);
        result.setMatchScore(0.9);

        when(pinyinSearchService.searchByInitials("AC")).thenReturn(List.of(result));

        ResponseEntity<List<SearchResultDTO<CustomerInfoDTO>>> response =
                pinyinSearchController.searchByInitials("AC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("initials", response.getBody().get(0).getMatchType());
    }

    @Test
    void testSearchByFullPinyin() {
        CustomerInfoDTO customer = new CustomerInfoDTO();
        customer.setId(1L);
        SearchResultDTO<CustomerInfoDTO> result = new SearchResultDTO<>();
        result.setMatchType("full");
        result.setData(customer);

        when(pinyinSearchService.searchByFullPinyin("acme")).thenReturn(List.of(result));

        ResponseEntity<List<SearchResultDTO<CustomerInfoDTO>>> response =
                pinyinSearchController.searchByFullPinyin("acme");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testSearchByMixedPinyin() {
        when(pinyinSearchService.searchByMixedPinyin("acme")).thenReturn(List.of());

        ResponseEntity<List<SearchResultDTO<CustomerInfoDTO>>> response =
                pinyinSearchController.searchByMixedPinyin("acme");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testRebuildPinyinIndex() {
        doNothing().when(pinyinSearchService).rebuildPinyinIndex();

        ResponseEntity<String> response = pinyinSearchController.rebuildPinyinIndex();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(pinyinSearchService).rebuildPinyinIndex();
    }
}
