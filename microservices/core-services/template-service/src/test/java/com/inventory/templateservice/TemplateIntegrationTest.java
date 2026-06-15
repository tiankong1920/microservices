package com.inventory.templateservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.common.template.BusinessDomain;
import com.inventory.common.template.TemplateStatus;
import com.inventory.common.template.dto.TemplateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestCacheConfig.class})
@Transactional
class TemplateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private TemplateDTO testTemplateDTO;

    @BeforeEach
    void setUp() {
        testTemplateDTO = TemplateDTO.builder()
                .templateCode("TPL_INTEGRATION_001")
                .templateName("Integration Test Template")
                .description("Integration test description")
                .businessDomain(BusinessDomain.CUSTOMER_MANAGEMENT)
                .status(TemplateStatus.DRAFT)
                .version("1.0.0")
                .tenantId("default")
                .allowCustomFields(true)
                .build();
    }

    private Long extractIdFromResponse(MvcResult result) throws Exception {
        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    @DisplayName("完整的模板生命周期测试")
    void templateLifecycleTest() throws Exception {
        String templateJson = objectMapper.writeValueAsString(testTemplateDTO);

        MvcResult createResult = mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(templateJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.templateCode").value("TPL_INTEGRATION_001"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        Long templateId = extractIdFromResponse(createResult);

        mockMvc.perform(get("/api/templates/{id}", templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateName").value("Integration Test Template"));

        TemplateDTO updateDTO = TemplateDTO.builder()
                .templateCode("TPL_INTEGRATION_001")
                .templateName("Updated Integration Template")
                .description("Updated description")
                .businessDomain(BusinessDomain.CUSTOMER_MANAGEMENT)
                .status(TemplateStatus.DRAFT)
                .tenantId("default")
                .build();

        mockMvc.perform(put("/api/templates/{id}", templateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateName").value("Updated Integration Template"));

        mockMvc.perform(get("/api/templates/code/{code}", "TPL_INTEGRATION_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateCode").value("TPL_INTEGRATION_001"));
    }

    @Test
    @DisplayName("模板搜索和过滤测试")
    void searchAndFilterTemplates() throws Exception {
        TemplateDTO template1 = TemplateDTO.builder()
                .templateCode("TPL_SEARCH_001")
                .templateName("Search Template 1")
                .businessDomain(BusinessDomain.INVENTORY_MANAGEMENT)
                .status(TemplateStatus.DRAFT)
                .tenantId("default")
                .build();

        TemplateDTO template2 = TemplateDTO.builder()
                .templateCode("TPL_SEARCH_002")
                .templateName("Search Template 2")
                .businessDomain(BusinessDomain.INVENTORY_MANAGEMENT)
                .status(TemplateStatus.PUBLISHED)
                .tenantId("default")
                .build();

        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/templates/domain/{domain}", "INVENTORY_MANAGEMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));

        mockMvc.perform(get("/api/templates")
                        .param("domain", "INVENTORY_MANAGEMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("模板验证测试")
    void validateTemplateTest() throws Exception {
        TemplateDTO invalidTemplate = TemplateDTO.builder()
                .templateCode("")
                .templateName("")
                .businessDomain(null)
                .tenantId("default")
                .build();

        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTemplate)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("重复模板编码测试")
    void duplicateTemplateCodeTest() throws Exception {
        TemplateDTO template = TemplateDTO.builder()
                .templateCode("TPL_DUP_001")
                .templateName("Duplicate Test")
                .businessDomain(BusinessDomain.CUSTOMER_MANAGEMENT)
                .status(TemplateStatus.DRAFT)
                .tenantId("default")
                .build();

        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("模板不存在测试")
    void templateNotFoundTest() throws Exception {
        mockMvc.perform(get("/api/templates/{id}", 99999L))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/templates/code/{code}", "NON_EXISTENT_CODE"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("模板状态转换测试")
    void templateStatusTransitionTest() throws Exception {
        TemplateDTO template = TemplateDTO.builder()
                .templateCode("TPL_STATUS_001")
                .templateName("Status Test Template")
                .businessDomain(BusinessDomain.CUSTOMER_MANAGEMENT)
                .status(TemplateStatus.DRAFT)
                .tenantId("default")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isCreated())
                .andReturn();

        Long templateId = extractIdFromResponse(createResult);

        mockMvc.perform(post("/api/templates/{id}/deprecate", templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEPRECATED"));

        mockMvc.perform(post("/api/templates/{id}/archive", templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }
}
