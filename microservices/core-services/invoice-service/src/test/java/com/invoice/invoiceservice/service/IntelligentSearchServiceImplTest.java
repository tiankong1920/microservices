package com.invoice.invoiceservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.invoice.invoiceservice.dto.CustomerInfoDTO;
import com.invoice.invoiceservice.dto.SearchResultDTO;
import com.invoice.invoiceservice.entity.CustomerInfo;
import com.invoice.invoiceservice.entity.PinyinIndex;
import com.invoice.invoiceservice.repository.ICustomerInfoRepository;
import com.invoice.invoiceservice.repository.IPinyinIndexRepository;
import com.invoice.invoiceservice.service.impl.IntelligentSearchServiceImpl;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntelligentSearchServiceImplTest {

    @Mock
    private ICustomerInfoRepository customerInfoRepository;

    @Mock
    private IPinyinIndexRepository pinyinIndexRepository;

    private IntelligentSearchService intelligentSearchService;

    private AesEncryptionUtil aesEncryptionUtil;

    @BeforeEach
    void setUp() {
        aesEncryptionUtil = new AesEncryptionUtil("testEncryptionKey12345678901234567");
        PinyinUtil pinyinUtil = new PinyinUtil();
        intelligentSearchService = new IntelligentSearchServiceImpl(
                customerInfoRepository, pinyinIndexRepository, aesEncryptionUtil, pinyinUtil);
    }

    private CustomerInfo createCustomer(Long id, String name, String taxNumber, String address,
                                        CustomerInfo.CustomerStatus status, String pinyinInitials,
                                        String pinyinFull, Integer usageCount, LocalDateTime lastUsedAt) {
        CustomerInfo c = new CustomerInfo();
        c.setId(id);
        c.setCustomerName(name);
        c.setTaxNumber(aesEncryptionUtil.encrypt(taxNumber));
        c.setRegisteredAddress(address);
        c.setStatus(status);
        c.setPinyinInitials(pinyinInitials);
        c.setPinyinFull(pinyinFull);
        c.setUsageCount(usageCount);
        c.setLastUsedAt(lastUsedAt);
        c.setBankAccount(aesEncryptionUtil.encrypt("6222021234567890123"));
        c.setDeleted(false);
        return c;
    }

    @Test
    void testFuzzySearchCustomersWithHighSimilarity() {
        CustomerInfo c1 = createCustomer(1L, "北京科技有限公司", "91110000MA01B1234X", "北京市朝阳区",
                CustomerInfo.CustomerStatus.ENABLED, "bj", "beijing", 5, LocalDateTime.now());

        when(customerInfoRepository.findByDeletedFalseAndStatus(CustomerInfo.CustomerStatus.ENABLED))
                .thenReturn(List.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                intelligentSearchService.fuzzySearchCustomers("北京科技", 0.5);

        assertFalse(results.isEmpty());
        assertEquals("北京科技有限公司", results.get(0).getData().getCustomerName());
        assertTrue(results.get(0).getMatchScore() >= 0.5);
        assertEquals("FUZZY", results.get(0).getMatchType());
    }

    @Test
    void testFuzzySearchCustomersNoMatch() {
        CustomerInfo c1 = createCustomer(1L, "上海贸易有限公司", "91310000MA01B5678Y", "上海市浦东新区",
                CustomerInfo.CustomerStatus.ENABLED, "sh", "shanghai", 3, LocalDateTime.now());

        when(customerInfoRepository.findByDeletedFalseAndStatus(CustomerInfo.CustomerStatus.ENABLED))
                .thenReturn(List.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                intelligentSearchService.fuzzySearchCustomers("ZZZZZZZZZ", 0.9);

        assertTrue(results.isEmpty());
    }

    @Test
    void testFuzzySearchCustomersEmptyResult() {
        when(customerInfoRepository.findByDeletedFalseAndStatus(CustomerInfo.CustomerStatus.ENABLED))
                .thenReturn(List.of());

        List<SearchResultDTO<CustomerInfoDTO>> results =
                intelligentSearchService.fuzzySearchCustomers("测试", 0.5);

        assertTrue(results.isEmpty());
    }

    @Test
    void testFuzzySearchCustomersWithAddressMatch() {
        CustomerInfo c1 = createCustomer(1L, "测试公司", "91110000MA01B1234X", "北京市朝阳区建国路",
                CustomerInfo.CustomerStatus.ENABLED, "cs", "ceshi", 2, LocalDateTime.now());

        when(customerInfoRepository.findByDeletedFalseAndStatus(CustomerInfo.CustomerStatus.ENABLED))
                .thenReturn(List.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                intelligentSearchService.fuzzySearchCustomers("北京市朝阳", 0.3);

        assertFalse(results.isEmpty());
    }

    @Test
    void testSearchByPinyin() {
        CustomerInfo c1 = createCustomer(1L, "北京科技有限公司", "91110000MA01B1234X", "北京市",
                CustomerInfo.CustomerStatus.ENABLED, "bj", "beijing", 5, LocalDateTime.now());

        PinyinIndex pi1 = new PinyinIndex();
        pi1.setEntityType("CUSTOMER_INFO");
        pi1.setEntityId(1L);
        pi1.setPinyinInitials("bj");
        pi1.setPinyinFull("beijing");

        when(pinyinIndexRepository.searchByPinyin("bj", "bj")).thenReturn(List.of(pi1));
        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                intelligentSearchService.searchByPinyin("bj");

        assertFalse(results.isEmpty());
        assertEquals(1.0, results.get(0).getMatchScore());
        assertEquals("PINYIN_FULL", results.get(0).getMatchType());
    }

    @Test
    void testSearchByPinyinDisabledCustomer() {
        CustomerInfo c1 = createCustomer(1L, "北京科技有限公司", "91110000MA01B1234X", "北京市",
                CustomerInfo.CustomerStatus.DISABLED, "bj", "beijing", 5, LocalDateTime.now());

        PinyinIndex pi1 = new PinyinIndex();
        pi1.setEntityType("CUSTOMER_INFO");
        pi1.setEntityId(1L);
        pi1.setPinyinInitials("bj");
        pi1.setPinyinFull("beijing");

        when(pinyinIndexRepository.searchByPinyin("bj", "bj")).thenReturn(List.of(pi1));
        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                intelligentSearchService.searchByPinyin("bj");

        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchByPinyinNonCustomerEntityType() {
        PinyinIndex pi1 = new PinyinIndex();
        pi1.setEntityType("PRODUCT");
        pi1.setEntityId(1L);
        pi1.setPinyinInitials("bj");
        pi1.setPinyinFull("beijing");

        when(pinyinIndexRepository.searchByPinyin("bj", "bj")).thenReturn(List.of(pi1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                intelligentSearchService.searchByPinyin("bj");

        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchByPinyinInitials() {
        CustomerInfo c1 = createCustomer(1L, "北京科技有限公司", "91110000MA01B1234X", "北京市",
                CustomerInfo.CustomerStatus.ENABLED, "bj", "beijing", 5, LocalDateTime.now());

        PinyinIndex pi1 = new PinyinIndex();
        pi1.setEntityType("CUSTOMER_INFO");
        pi1.setEntityId(1L);
        pi1.setPinyinInitials("bj");
        pi1.setPinyinFull("beijing");

        when(pinyinIndexRepository.findByPinyinInitialsStartingWith("bj")).thenReturn(List.of(pi1));
        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                intelligentSearchService.searchByPinyinInitials("bj");

        assertFalse(results.isEmpty());
        assertEquals("PINYIN_INITIALS", results.get(0).getMatchType());
    }

    @Test
    void testSearchByPinyinInitialsNotFound() {
        when(pinyinIndexRepository.findByPinyinInitialsStartingWith("zz")).thenReturn(List.of());

        List<SearchResultDTO<CustomerInfoDTO>> results =
                intelligentSearchService.searchByPinyinInitials("zz");

        assertTrue(results.isEmpty());
    }

    @Test
    void testCompositeSearchWithKeywordOnly() {
        CustomerInfo c1 = createCustomer(1L, "北京科技有限公司", "91110000MA01B1234X", "北京市",
                CustomerInfo.CustomerStatus.ENABLED, "bj", "beijing", 5, LocalDateTime.now());

        when(customerInfoRepository.searchByKeywordOrderByUsage("北京")).thenReturn(List.of(c1));
        when(customerInfoRepository.findByDeletedFalseAndStatus(CustomerInfo.CustomerStatus.ENABLED))
                .thenReturn(List.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                intelligentSearchService.compositeSearch("北京", null, 0.5);

        assertFalse(results.isEmpty());
    }

    @Test
    void testCompositeSearchWithPinyinOnly() {
        CustomerInfo c1 = createCustomer(1L, "北京科技有限公司", "91110000MA01B1234X", "北京市",
                CustomerInfo.CustomerStatus.ENABLED, "bj", "beijing", 5, LocalDateTime.now());

        PinyinIndex pi1 = new PinyinIndex();
        pi1.setEntityType("CUSTOMER_INFO");
        pi1.setEntityId(1L);
        pi1.setPinyinInitials("bj");
        pi1.setPinyinFull("beijing");

        when(pinyinIndexRepository.searchByPinyin("bj", "bj")).thenReturn(List.of(pi1));
        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(c1));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                intelligentSearchService.compositeSearch(null, "bj", 0.5);

        assertFalse(results.isEmpty());
    }

    @Test
    void testCompositeSearchWithBothKeywordAndPinyin() {
        CustomerInfo c1 = createCustomer(1L, "北京科技有限公司", "91110000MA01B1234X", "北京市",
                CustomerInfo.CustomerStatus.ENABLED, "bj", "beijing", 5, LocalDateTime.now());
        CustomerInfo c2 = createCustomer(2L, "上海贸易有限公司", "91310000MA01B5678Y", "上海市",
                CustomerInfo.CustomerStatus.ENABLED, "sh", "shanghai", 3, LocalDateTime.now());

        PinyinIndex pi2 = new PinyinIndex();
        pi2.setEntityType("CUSTOMER_INFO");
        pi2.setEntityId(2L);
        pi2.setPinyinInitials("sh");
        pi2.setPinyinFull("shanghai");

        when(customerInfoRepository.searchByKeywordOrderByUsage("北京")).thenReturn(List.of(c1));
        when(customerInfoRepository.findByDeletedFalseAndStatus(CustomerInfo.CustomerStatus.ENABLED))
                .thenReturn(List.of(c1, c2));
        when(pinyinIndexRepository.searchByPinyin("sh", "sh")).thenReturn(List.of(pi2));
        when(customerInfoRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.of(c2));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                intelligentSearchService.compositeSearch("北京", "sh", 0.5);

        assertFalse(results.isEmpty());
        assertTrue(results.size() >= 2);
    }

    @Test
    void testCompositeSearchEmptyInputs() {
        List<SearchResultDTO<CustomerInfoDTO>> results =
                intelligentSearchService.compositeSearch("", "", 0.5);

        assertTrue(results.isEmpty());
    }

    @Test
    void testGetFrequentlyUsedCustomers() {
        CustomerInfo c1 = createCustomer(1L, "公司A", "91110000MA01B1234X", "北京市",
                CustomerInfo.CustomerStatus.ENABLED, "gs", "gongsi", 10, LocalDateTime.now());
        CustomerInfo c2 = createCustomer(2L, "公司B", "91310000MA01B5678Y", "上海市",
                CustomerInfo.CustomerStatus.ENABLED, "gs", "gongsi", 5, LocalDateTime.now());

        when(customerInfoRepository.findAllOrderByUsageDesc()).thenReturn(List.of(c1, c2));

        List<CustomerInfoDTO> results = intelligentSearchService.getFrequentlyUsedCustomers(5);

        assertEquals(2, results.size());
        assertEquals("公司A", results.get(0).getCustomerName());
    }

    @Test
    void testGetFrequentlyUsedCustomersWithLimit() {
        CustomerInfo c1 = createCustomer(1L, "公司A", "91110000MA01B1234X", "北京市",
                CustomerInfo.CustomerStatus.ENABLED, "gs", "gongsi", 10, LocalDateTime.now());
        CustomerInfo c2 = createCustomer(2L, "公司B", "91310000MA01B5678Y", "上海市",
                CustomerInfo.CustomerStatus.ENABLED, "gs", "gongsi", 5, LocalDateTime.now());

        when(customerInfoRepository.findAllOrderByUsageDesc()).thenReturn(List.of(c1, c2));

        List<CustomerInfoDTO> results = intelligentSearchService.getFrequentlyUsedCustomers(1);

        assertEquals(1, results.size());
    }

    @Test
    void testGetRecentlyUsedCustomers() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earlier = now.minusDays(1);

        CustomerInfo c1 = createCustomer(1L, "公司A", "91110000MA01B1234X", "北京市",
                CustomerInfo.CustomerStatus.ENABLED, "gs", "gongsi", 10, now);
        CustomerInfo c2 = createCustomer(2L, "公司B", "91310000MA01B5678Y", "上海市",
                CustomerInfo.CustomerStatus.ENABLED, "gs", "gongsi", 5, earlier);

        when(customerInfoRepository.findByDeletedFalseAndStatus(CustomerInfo.CustomerStatus.ENABLED))
                .thenReturn(List.of(c1, c2));

        List<CustomerInfoDTO> results = intelligentSearchService.getRecentlyUsedCustomers(5);

        assertEquals(2, results.size());
        assertEquals("公司A", results.get(0).getCustomerName());
    }

    @Test
    void testGetRecentlyUsedCustomersFiltersNullLastUsedAt() {
        CustomerInfo c1 = createCustomer(1L, "公司A", "91110000MA01B1234X", "北京市",
                CustomerInfo.CustomerStatus.ENABLED, "gs", "gongsi", 10, LocalDateTime.now());
        CustomerInfo c2 = createCustomer(2L, "公司B", "91310000MA01B5678Y", "上海市",
                CustomerInfo.CustomerStatus.ENABLED, "gs", "gongsi", 5, null);

        when(customerInfoRepository.findByDeletedFalseAndStatus(CustomerInfo.CustomerStatus.ENABLED))
                .thenReturn(List.of(c1, c2));

        List<CustomerInfoDTO> results = intelligentSearchService.getRecentlyUsedCustomers(5);

        assertEquals(1, results.size());
        assertEquals("公司A", results.get(0).getCustomerName());
    }

    @Test
    void testMapToDTODecryptsSensitiveFields() {
        CustomerInfo c1 = createCustomer(1L, "测试公司", "91110000MA01B1234X", "北京市",
                CustomerInfo.CustomerStatus.ENABLED, "cs", "ceshi", 1, LocalDateTime.now());

        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(c1));

        PinyinIndex pi = new PinyinIndex();
        pi.setEntityType("CUSTOMER_INFO");
        pi.setEntityId(1L);
        pi.setPinyinInitials("cs");
        pi.setPinyinFull("ceshi");

        when(pinyinIndexRepository.searchByPinyin("cs", "cs")).thenReturn(List.of(pi));

        List<SearchResultDTO<CustomerInfoDTO>> results =
                intelligentSearchService.searchByPinyin("cs");

        assertFalse(results.isEmpty());
        assertNotNull(results.get(0).getData().getTaxNumber());
        assertNotNull(results.get(0).getData().getBankAccount());
    }
}
