package com.inventory.templateservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.common.template.BusinessDomain;
import com.inventory.common.template.TemplateStatus;
import com.inventory.templateservice.entity.Template;
import com.inventory.templateservice.repository.ITemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestCacheConfig.class})
@Transactional
class TemplateStatisticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ITemplateRepository templateRepository;

    @BeforeEach
    void setUp() {
        createTestTemplates();
    }

    private void createTestTemplates() {
        for (int i = 0; i < 5; i++) {
            Template template = Template.builder()
                    .templateCode("TPL_STATS_" + String.format("%03d", i))
                    .name("Statistics Test Template " + i)
                    .businessDomain(i % 2 == 0 ? BusinessDomain.CUSTOMER_MANAGEMENT : BusinessDomain.INVENTORY_MANAGEMENT)
                    .status(i % 3 == 0 ? TemplateStatus.PUBLISHED : TemplateStatus.DRAFT)
                    .tenantId("default")
                    .build();
            templateRepository.save(template);
        }
    }

    @Test
    @DisplayName("按领域查询模板统计")
    void getTemplatesByDomain() throws Exception {
        mockMvc.perform(get("/api/templates/domain/{domain}", "CUSTOMER_MANAGEMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("按状态查询模板统计")
    void getTemplatesByStatus() throws Exception {
        mockMvc.perform(get("/api/templates/status/{status}", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("搜索模板")
    void searchTemplates() throws Exception {
        mockMvc.perform(get("/api/templates")
                        .param("keyword", "Statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("按领域统计模板数量")
    void countTemplatesByDomain() throws Exception {
        long customerCount = templateRepository.findByBusinessDomain(BusinessDomain.CUSTOMER_MANAGEMENT).size();
        long inventoryCount = templateRepository.findByBusinessDomain(BusinessDomain.INVENTORY_MANAGEMENT).size();

        org.assertj.core.api.Assertions.assertThat(customerCount).isGreaterThanOrEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(inventoryCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("按状态统计模板数量")
    void countTemplatesByStatus() throws Exception {
        long draftCount = templateRepository.findByStatus(TemplateStatus.DRAFT).size();
        long publishedCount = templateRepository.findByStatus(TemplateStatus.PUBLISHED).size();

        org.assertj.core.api.Assertions.assertThat(draftCount).isGreaterThanOrEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(publishedCount).isGreaterThanOrEqualTo(1);
    }
}
