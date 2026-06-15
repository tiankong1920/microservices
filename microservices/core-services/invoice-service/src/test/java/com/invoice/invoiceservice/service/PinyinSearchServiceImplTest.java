package com.invoice.invoiceservice.service;

import java.util.List;
import java.util.Optional;

import com.invoice.invoiceservice.dto.CustomerInfoDTO;
import com.invoice.invoiceservice.dto.SearchResultDTO;
import com.invoice.invoiceservice.entity.CustomerInfo;
import com.invoice.invoiceservice.entity.PinyinIndex;
import com.invoice.invoiceservice.repository.ICustomerInfoRepository;
import com.invoice.invoiceservice.repository.IPinyinIndexRepository;
import com.invoice.invoiceservice.service.impl.PinyinSearchServiceImpl;
import com.invoice.invoiceservice.util.AesEncryptionUtil;
import com.invoice.invoiceservice.util.PinyinUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PinyinSearchServiceImplTest {

    @Mock
    private ICustomerInfoRepository customerInfoRepository;

    @Mock
    private IPinyinIndexRepository pinyinIndexRepository;

    private PinyinSearchService pinyinSearchService;

    private AesEncryptionUtil aesEncryptionUtil;

    @BeforeEach
    void setUp() {
        aesEncryptionUtil = new AesEncryptionUtil("testEncryptionKey12345678901234567");
        PinyinUtil pinyinUtil = new PinyinUtil();
        pinyinSearchService = new PinyinSearchServiceImpl(
                pinyinIndexRepository, customerInfoRepository, aesEncryptionUtil, pinyinUtil);
    }

    private CustomerInfo createCustomer(Long id, String name, String taxNumber,
                                        CustomerInfo.CustomerStatus status,
                                        String pinyinInitials, String pinyinFull,
                                        Integer usageCount) {
        CustomerInfo c = new CustomerInfo();
        c.setId(id);
        c.setCustomerName(name);
        c.setTaxNumber(aesEncryptionUtil.encrypt(taxNumber));
        c.setStatus(status);
        c.setPinyinInitials(pinyinInitials);
        c.setPinyinFull(pinyinFull);
        c.setUsageCount(usageCount);
        c.setBankAccount(aesEncryptionUtil.encrypt("6222021234567890123"));
        c.setDeleted(false);
        c.setTenantId("default");
        return c;
    }

    @Test
    void testSearchByInitials() {
        CustomerInfo c1 = createCustomer(1L, "北京科技有限公司", "91110000MA01B1234X",
                CustomerInfo.CustomerStatus.ENABLED, "bj", "beijing", 5);

        PinyinIndex pi1 = new PinyinIndex();
        pi1.setEntityType("CUSTOMER_INFO");
        pi1.setEntityId(1L);
        pi1.setPinyinInitials("bj");
        pi1.setPinyinFull("beijing");

        when(pinyinIndexRepository.findByPinyinInitialsStartingWith("bj")).thenReturn(List.of(pi1));
        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                pinyinSearchService.searchByInitials("bj");

        assertFalse(results.isEmpty());
        assertEquals("PINYIN_INITIALS", results.get(0).getMatchType());
        assertTrue(results.get(0).getMatchScore() > 0);
    }

    @Test
    void testSearchByInitialsExactMatch() {
        CustomerInfo c1 = createCustomer(1L, "北京科技有限公司", "91110000MA01B1234X",
                CustomerInfo.CustomerStatus.ENABLED, "bj", "beijing", 5);

        PinyinIndex pi1 = new PinyinIndex();
        pi1.setEntityType("CUSTOMER_INFO");
        pi1.setEntityId(1L);
        pi1.setPinyinInitials("bj");
        pi1.setPinyinFull("beijing");

        when(pinyinIndexRepository.findByPinyinInitialsStartingWith("bj")).thenReturn(List.of(pi1));
        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                pinyinSearchService.searchByInitials("bj");

        assertFalse(results.isEmpty());
        assertEquals(1.0, results.get(0).getMatchScore());
    }

    @Test
    void testSearchByInitialsPrefixMatch() {
        CustomerInfo c1 = createCustomer(1L, "北京科技有限公司", "91110000MA01B1234X",
                CustomerInfo.CustomerStatus.ENABLED, "bj", "beijing", 5);

        PinyinIndex pi1 = new PinyinIndex();
        pi1.setEntityType("CUSTOMER_INFO");
        pi1.setEntityId(1L);
        pi1.setPinyinInitials("bj");
        pi1.setPinyinFull("beijing");

        when(pinyinIndexRepository.findByPinyinInitialsStartingWith("b")).thenReturn(List.of(pi1));
        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                pinyinSearchService.searchByInitials("b");

        assertFalse(results.isEmpty());
        assertTrue(results.get(0).getMatchScore() >= 0.9);
    }

    @Test
    void testSearchByInitialsDisabledCustomer() {
        CustomerInfo c1 = createCustomer(1L, "北京科技有限公司", "91110000MA01B1234X",
                CustomerInfo.CustomerStatus.DISABLED, "bj", "beijing", 5);

        PinyinIndex pi1 = new PinyinIndex();
        pi1.setEntityType("CUSTOMER_INFO");
        pi1.setEntityId(1L);
        pi1.setPinyinInitials("bj");
        pi1.setPinyinFull("beijing");

        when(pinyinIndexRepository.findByPinyinInitialsStartingWith("bj")).thenReturn(List.of(pi1));
        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                pinyinSearchService.searchByInitials("bj");

        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchByInitialsNonCustomerEntityType() {
        PinyinIndex pi1 = new PinyinIndex();
        pi1.setEntityType("PRODUCT");
        pi1.setEntityId(1L);
        pi1.setPinyinInitials("bj");

        when(pinyinIndexRepository.findByPinyinInitialsStartingWith("bj")).thenReturn(List.of(pi1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                pinyinSearchService.searchByInitials("bj");

        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchByInitialsNotFound() {
        when(pinyinIndexRepository.findByPinyinInitialsStartingWith("zz")).thenReturn(List.of());

        List<SearchResultDTO<CustomerInfoDTO>> results =
                pinyinSearchService.searchByInitials("zz");

        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchByFullPinyin() {
        CustomerInfo c1 = createCustomer(1L, "北京科技有限公司", "91110000MA01B1234X",
                CustomerInfo.CustomerStatus.ENABLED, "bj", "beijing", 5);

        PinyinIndex pi1 = new PinyinIndex();
        pi1.setEntityType("CUSTOMER_INFO");
        pi1.setEntityId(1L);
        pi1.setPinyinInitials("bj");
        pi1.setPinyinFull("beijing");

        when(pinyinIndexRepository.searchByPinyin("beijing", "beijing")).thenReturn(List.of(pi1));
        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                pinyinSearchService.searchByFullPinyin("beijing");

        assertFalse(results.isEmpty());
        assertEquals(1.0, results.get(0).getMatchScore());
        assertEquals("PINYIN_FULL", results.get(0).getMatchType());
    }

    @Test
    void testSearchByFullPinyinSortedByUsageCount() {
        CustomerInfo c1 = createCustomer(1L, "北京公司A", "91110000MA01B1234X",
                CustomerInfo.CustomerStatus.ENABLED, "bj", "beijing", 3);
        CustomerInfo c2 = createCustomer(2L, "北京公司B", "91310000MA01B5678Y",
                CustomerInfo.CustomerStatus.ENABLED, "bj", "beijing", 10);

        PinyinIndex pi1 = new PinyinIndex();
        pi1.setEntityType("CUSTOMER_INFO");
        pi1.setEntityId(1L);
        pi1.setPinyinInitials("bj");
        pi1.setPinyinFull("beijing");

        PinyinIndex pi2 = new PinyinIndex();
        pi2.setEntityType("CUSTOMER_INFO");
        pi2.setEntityId(2L);
        pi2.setPinyinInitials("bj");
        pi2.setPinyinFull("beijing");

        when(pinyinIndexRepository.searchByPinyin("beijing", "beijing")).thenReturn(List.of(pi1, pi2));
        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(c1));
        when(customerInfoRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.of(c2));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                pinyinSearchService.searchByFullPinyin("beijing");

        assertEquals(2, results.size());
        assertEquals(10, results.get(0).getData().getUsageCount());
    }

    @Test
    void testSearchByFullPinyinEmptyResult() {
        when(pinyinIndexRepository.searchByPinyin("zzz", "zzz")).thenReturn(List.of());

        List<SearchResultDTO<CustomerInfoDTO>> results =
                pinyinSearchService.searchByFullPinyin("zzz");

        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchByMixedPinyin() {
        CustomerInfo c1 = createCustomer(1L, "北京科技有限公司", "91110000MA01B1234X",
                CustomerInfo.CustomerStatus.ENABLED, "bj", "beijing", 5);

        PinyinIndex pi1 = new PinyinIndex();
        pi1.setEntityType("CUSTOMER_INFO");
        pi1.setEntityId(1L);
        pi1.setPinyinInitials("bj");
        pi1.setPinyinFull("beijing");

        when(pinyinIndexRepository.findByPinyinInitialsStartingWith("bj")).thenReturn(List.of(pi1));
        when(pinyinIndexRepository.searchByPinyin("bj", "bj")).thenReturn(List.of(pi1));
        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                pinyinSearchService.searchByMixedPinyin("bj");

        assertFalse(results.isEmpty());
    }

    @Test
    void testSearchByMixedPinyinDeduplicatesResults() {
        CustomerInfo c1 = createCustomer(1L, "北京科技有限公司", "91110000MA01B1234X",
                CustomerInfo.CustomerStatus.ENABLED, "bj", "beijing", 5);

        PinyinIndex pi1 = new PinyinIndex();
        pi1.setEntityType("CUSTOMER_INFO");
        pi1.setEntityId(1L);
        pi1.setPinyinInitials("bj");
        pi1.setPinyinFull("beijing");

        when(pinyinIndexRepository.findByPinyinInitialsStartingWith("bj")).thenReturn(List.of(pi1));
        when(pinyinIndexRepository.searchByPinyin("bj", "bj")).thenReturn(List.of(pi1));
        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                pinyinSearchService.searchByMixedPinyin("bj");

        long uniqueIds = results.stream()
                .map(r -> r.getData().getId())
                .distinct()
                .count();
        assertEquals(results.size(), uniqueIds);
    }

    @Test
    void testRebuildPinyinIndex() {
        CustomerInfo c1 = createCustomer(1L, "北京公司", "91110000MA01B1234X",
                CustomerInfo.CustomerStatus.ENABLED, null, null, 0);

        when(customerInfoRepository.findByDeletedFalse()).thenReturn(List.of(c1));
        when(customerInfoRepository.save(any(CustomerInfo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pinyinIndexRepository.save(any(PinyinIndex.class))).thenAnswer(inv -> inv.getArgument(0));

        pinyinSearchService.rebuildPinyinIndex();

        verify(customerInfoRepository).save(any(CustomerInfo.class));
        verify(pinyinIndexRepository).deleteByEntityTypeAndEntityId("CUSTOMER_INFO", 1L);
        verify(pinyinIndexRepository).save(any(PinyinIndex.class));
    }

    @Test
    void testRebuildPinyinIndexEmptyCustomers() {
        when(customerInfoRepository.findByDeletedFalse()).thenReturn(List.of());

        pinyinSearchService.rebuildPinyinIndex();

        verify(customerInfoRepository).findByDeletedFalse();
    }

    @Test
    void testMapToDTODecryptsSensitiveFields() {
        CustomerInfo c1 = createCustomer(1L, "测试公司", "91110000MA01B1234X",
                CustomerInfo.CustomerStatus.ENABLED, "cs", "ceshi", 1);

        PinyinIndex pi1 = new PinyinIndex();
        pi1.setEntityType("CUSTOMER_INFO");
        pi1.setEntityId(1L);
        pi1.setPinyinInitials("cs");
        pi1.setPinyinFull("ceshi");

        when(pinyinIndexRepository.findByPinyinInitialsStartingWith("cs")).thenReturn(List.of(pi1));
        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                pinyinSearchService.searchByInitials("cs");

        assertFalse(results.isEmpty());
        CustomerInfoDTO dto = results.get(0).getData();
        assertNotNull(dto.getTaxNumber());
        assertNotNull(dto.getBankAccount());
    }
}
