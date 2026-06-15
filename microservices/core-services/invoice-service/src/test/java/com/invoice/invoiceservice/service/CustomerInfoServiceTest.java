package com.invoice.invoiceservice.service;

import com.invoice.invoiceservice.dto.CustomerInfoDTO;
import com.invoice.invoiceservice.entity.CustomerInfo;
import com.invoice.invoiceservice.entity.OperationLog;
import com.invoice.invoiceservice.exception.CustomerInfoNotFoundException;
import com.invoice.invoiceservice.exception.DuplicateTaxNumberException;
import com.invoice.invoiceservice.repository.ICustomerInfoRepository;
import com.invoice.invoiceservice.repository.IOperationLogRepository;
import com.invoice.invoiceservice.repository.IPinyinIndexRepository;
import com.invoice.invoiceservice.service.impl.CustomerInfoServiceImpl;
import com.invoice.invoiceservice.util.AesEncryptionUtil;
import com.invoice.invoiceservice.util.PinyinUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerInfoServiceTest {

    @Mock
    private ICustomerInfoRepository customerInfoRepository;

    @Mock
    private IOperationLogRepository operationLogRepository;

    @Mock
    private IPinyinIndexRepository pinyinIndexRepository;

    private CustomerInfoService customerInfoService;

    @BeforeEach
    void setUp() {
        AesEncryptionUtil aesEncryptionUtil = new AesEncryptionUtil("testEncryptionKey12345678901234567");
        PinyinUtil pinyinUtil = new PinyinUtil();
        customerInfoService = new CustomerInfoServiceImpl(
                customerInfoRepository, operationLogRepository, pinyinIndexRepository,
                aesEncryptionUtil, pinyinUtil);
    }

    @Test
    void testCreateCustomer() {
        CustomerInfoDTO dto = CustomerInfoDTO.builder()
                .customerName("北京科技有限公司")
                .taxNumber("91110000MA01B1234X")
                .registeredAddress("北京市朝阳区")
                .mobilePhone("13800138000")
                .bankName("中国银行")
                .bankAccount("6222021234567890123")
                .tenantId("default")
                .createdBy("admin")
                .build();

        when(customerInfoRepository.existsByTaxNumberAndDeletedFalse(anyString())).thenReturn(false);
        when(customerInfoRepository.save(any(CustomerInfo.class))).thenAnswer(invocation -> {
            CustomerInfo entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });
        when(operationLogRepository.save(any(OperationLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerInfoDTO result = customerInfoService.createCustomer(dto);

        assertNotNull(result);
        assertEquals("北京科技有限公司", result.getCustomerName());
        assertEquals("91110000MA01B1234X", result.getTaxNumber());
        verify(customerInfoRepository).save(any(CustomerInfo.class));
    }

    @Test
    void testCreateCustomerDuplicateTaxNumber() {
        CustomerInfoDTO dto = CustomerInfoDTO.builder()
                .customerName("测试公司")
                .taxNumber("91110000MA01B1234X")
                .build();

        when(customerInfoRepository.existsByTaxNumberAndDeletedFalse("91110000MA01B1234X")).thenReturn(true);

        assertThrows(DuplicateTaxNumberException.class, () -> customerInfoService.createCustomer(dto));
    }

    @Test
    void testGetCustomerById() {
        AesEncryptionUtil aesUtil = new AesEncryptionUtil("testEncryptionKey12345678901234567");
        CustomerInfo entity = new CustomerInfo();
        entity.setId(1L);
        entity.setCustomerName("测试公司");
        entity.setTaxNumber(aesUtil.encrypt("91110000MA01B1234X"));
        entity.setStatus(CustomerInfo.CustomerStatus.ENABLED);

        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(entity));

        CustomerInfoDTO result = customerInfoService.getCustomerById(1L);

        assertNotNull(result);
        assertEquals("测试公司", result.getCustomerName());
    }

    @Test
    void testGetCustomerByIdNotFound() {
        when(customerInfoRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerInfoNotFoundException.class, () -> customerInfoService.getCustomerById(999L));
    }

    @Test
    void testDeleteCustomer() {
        AesEncryptionUtil aesUtil = new AesEncryptionUtil("testEncryptionKey12345678901234567");
        CustomerInfo entity = new CustomerInfo();
        entity.setId(1L);
        entity.setCustomerName("测试公司");
        entity.setTaxNumber(aesUtil.encrypt("91110000MA01B1234X"));
        entity.setStatus(CustomerInfo.CustomerStatus.ENABLED);

        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(entity));
        when(customerInfoRepository.save(any(CustomerInfo.class))).thenReturn(entity);
        when(operationLogRepository.save(any(OperationLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customerInfoService.deleteCustomer(1L);

        verify(customerInfoRepository).save(any(CustomerInfo.class));
    }

    @Test
    void testEnableCustomer() {
        AesEncryptionUtil aesUtil = new AesEncryptionUtil("testEncryptionKey12345678901234567");
        CustomerInfo entity = new CustomerInfo();
        entity.setId(1L);
        entity.setCustomerName("测试公司");
        entity.setTaxNumber(aesUtil.encrypt("91110000MA01B1234X"));
        entity.setStatus(CustomerInfo.CustomerStatus.DISABLED);

        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(entity));
        when(customerInfoRepository.save(any(CustomerInfo.class))).thenReturn(entity);
        when(operationLogRepository.save(any(OperationLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customerInfoService.enableCustomer(1L);

        verify(customerInfoRepository).save(any(CustomerInfo.class));
    }

    @Test
    void testDisableCustomer() {
        AesEncryptionUtil aesUtil = new AesEncryptionUtil("testEncryptionKey12345678901234567");
        CustomerInfo entity = new CustomerInfo();
        entity.setId(1L);
        entity.setCustomerName("测试公司");
        entity.setTaxNumber(aesUtil.encrypt("91110000MA01B1234X"));
        entity.setStatus(CustomerInfo.CustomerStatus.ENABLED);

        when(customerInfoRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(entity));
        when(customerInfoRepository.save(any(CustomerInfo.class))).thenReturn(entity);
        when(operationLogRepository.save(any(OperationLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customerInfoService.disableCustomer(1L);

        verify(customerInfoRepository).save(any(CustomerInfo.class));
    }

    @Test
    void testGetAllCustomers() {
        AesEncryptionUtil aesUtil = new AesEncryptionUtil("testEncryptionKey12345678901234567");
        CustomerInfo entity1 = new CustomerInfo();
        entity1.setId(1L);
        entity1.setCustomerName("公司A");
        entity1.setTaxNumber(aesUtil.encrypt("91110000MA01B1234X"));
        entity1.setStatus(CustomerInfo.CustomerStatus.ENABLED);

        when(customerInfoRepository.findByDeletedFalse()).thenReturn(List.of(entity1));

        List<CustomerInfoDTO> results = customerInfoService.getAllCustomers();

        assertEquals(1, results.size());
    }
}
