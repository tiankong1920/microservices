package com.invoice.invoiceservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoice.invoiceservice.dto.CustomerInfoDTO;
import com.invoice.invoiceservice.dto.InvoiceProductDTO;
import com.invoice.invoiceservice.dto.UnitInfoDTO;
import com.invoice.invoiceservice.entity.CustomerInfo;
import com.invoice.invoiceservice.entity.InvoiceProduct;
import com.invoice.invoiceservice.entity.UnitInfo;
import com.invoice.invoiceservice.repository.ICustomerInfoRepository;
import com.invoice.invoiceservice.repository.IInvoiceProductRepository;
import com.invoice.invoiceservice.repository.IUnitInfoRepository;
import com.invoice.invoiceservice.util.AesEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class InvoiceServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ICustomerInfoRepository customerInfoRepository;

    @Autowired
    private IInvoiceProductRepository invoiceProductRepository;

    @Autowired
    private IUnitInfoRepository unitInfoRepository;

    @Autowired
    private AesEncryptionUtil aesEncryptionUtil;

    @BeforeEach
    void cleanUp() {
        customerInfoRepository.deleteAll();
        invoiceProductRepository.deleteAll();
        unitInfoRepository.deleteAll();
    }

    @Test
    void testCustomerInfoFullLifecycle() throws Exception {
        CustomerInfoDTO dto = CustomerInfoDTO.builder()
                .customerName("测试公司")
                .taxNumber("91110000MA01B1234X")
                .registeredAddress("北京市朝阳区")
                .contactPhone("010-12345678")
                .bankName("中国银行")
                .bankAccount("622588012345678901")
                .status("ENABLED")
                .build();

        mockMvc.perform(post("/api/invoice/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("测试公司"));

        mockMvc.perform(get("/api/invoice/customers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testUnitInfoFullLifecycle() throws Exception {
        UnitInfoDTO dto = UnitInfoDTO.builder()
                .unitName("个")
                .aliases("只,件")
                .build();

        mockMvc.perform(post("/api/invoice/units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitName").value("个"));

        mockMvc.perform(get("/api/invoice/units")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testInvoiceProductFullLifecycle() throws Exception {
        InvoiceProductDTO dto = InvoiceProductDTO.builder()
                .productName("测试商品")
                .specification("100ml")
                .unitName("个")
                .taxRate(new BigDecimal("0.13"))
                .unitPrice(new BigDecimal("100.00"))
                .productCategory("日用品")
                .build();

        mockMvc.perform(post("/api/invoice/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("测试商品"));
    }

    @Test
    void testGetCustomerById() throws Exception {
        CustomerInfo entity = new CustomerInfo();
        entity.setCustomerName("查询测试公司");
        entity.setTaxNumber(aesEncryptionUtil.encrypt("91110000MA01B1234X"));
        entity.setRegisteredAddress("北京市");
        entity.setStatus(CustomerInfo.CustomerStatus.ENABLED);
        entity.setDeleted(false);
        entity = customerInfoRepository.save(entity);

        mockMvc.perform(get("/api/invoice/customers/" + entity.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("查询测试公司"));
    }

    @Test
    void testGetUnitById() throws Exception {
        UnitInfo entity = new UnitInfo();
        entity.setUnitName("千克");
        entity.setDeleted(false);
        entity = unitInfoRepository.save(entity);

        mockMvc.perform(get("/api/invoice/units/" + entity.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitName").value("千克"));
    }

    @Test
    void testGetProductById() throws Exception {
        InvoiceProduct entity = new InvoiceProduct();
        entity.setProductName("查询测试商品");
        entity.setUnitName("瓶");
        entity.setTaxRate(new BigDecimal("0.13"));
        entity.setUnitPrice(new BigDecimal("50.00"));
        entity.setStatus(InvoiceProduct.ProductStatus.ENABLED);
        entity.setDeleted(false);
        entity = invoiceProductRepository.save(entity);

        mockMvc.perform(get("/api/invoice/products/" + entity.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("查询测试商品"));
    }
}
