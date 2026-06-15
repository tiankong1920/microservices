package com.inventory.templateservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.common.template.BusinessDomain;
import com.inventory.common.template.TemplateStatus;
import com.inventory.common.template.dto.TemplateDTO;
import com.inventory.common.template.dto.TemplateVersionDTO;
import com.inventory.common.template.service.ITemplateService;
import com.inventory.common.template.service.ITemplateVersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ITemplateService templateService;

    @MockitoBean
    private ITemplateVersionService versionService;

    private TemplateDTO testTemplateDTO;

    @BeforeEach
    void setUp() {
        testTemplateDTO = TemplateDTO.builder()
                .id(1L)
                .templateCode("TPL_TEST_001")
                .templateName("Test Template")
                .description("Test Description")
                .businessDomain(BusinessDomain.CUSTOMER_MANAGEMENT)
                .status(TemplateStatus.DRAFT)
                .version("1.0.0")
                .tenantId("default")
                .build();
    }

    @Test
    @DisplayName("创建模板 - 成功")
    void createTemplate_Success() throws Exception {
        when(templateService.createTemplate(any(TemplateDTO.class))).thenReturn(testTemplateDTO);

        mockMvc.perform(post("/api/v1/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTemplateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.templateCode").value("TPL_TEST_001"))
                .andExpect(jsonPath("$.templateName").value("Test Template"));
    }

    @Test
    @DisplayName("获取模板详情 - 成功")
    void getTemplate_Success() throws Exception {
        when(templateService.getTemplateById(anyLong())).thenReturn(Optional.of(testTemplateDTO));

        mockMvc.perform(get("/api/v1/templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateCode").value("TPL_TEST_001"));
    }

    @Test
    @DisplayName("获取模板详情 - 不存在")
    void getTemplate_NotFound() throws Exception {
        when(templateService.getTemplateById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/templates/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("根据编码获取模板 - 成功")
    void getTemplateByCode_Success() throws Exception {
        when(templateService.getTemplateByCode(anyString())).thenReturn(Optional.of(testTemplateDTO));

        mockMvc.perform(get("/api/v1/templates/code/TPL_TEST_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateCode").value("TPL_TEST_001"));
    }

    @Test
    @DisplayName("搜索模板")
    void searchTemplates() throws Exception {
        when(templateService.searchTemplates(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(testTemplateDTO));

        mockMvc.perform(get("/api/v1/templates")
                        .param("keyword", "test")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].templateCode").value("TPL_TEST_001"));
    }

    @Test
    @DisplayName("按领域获取模板")
    void getTemplatesByDomain() throws Exception {
        when(templateService.getTemplatesByDomain(anyString())).thenReturn(List.of(testTemplateDTO));

        mockMvc.perform(get("/api/v1/templates/domain/CUSTOMER_MANAGEMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].businessDomain").value("CUSTOMER_MANAGEMENT"));
    }

    @Test
    @DisplayName("更新模板 - 成功")
    void updateTemplate_Success() throws Exception {
        when(templateService.updateTemplate(anyLong(), any(TemplateDTO.class))).thenReturn(testTemplateDTO);

        mockMvc.perform(put("/api/v1/templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTemplateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateCode").value("TPL_TEST_001"));
    }

    @Test
    @DisplayName("删除模板 - 成功")
    void deleteTemplate_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/templates/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("发布模板 - 成功")
    void publishTemplate_Success() throws Exception {
        testTemplateDTO.setStatus(TemplateStatus.PUBLISHED);
        when(templateService.publishTemplate(anyLong())).thenReturn(testTemplateDTO);

        mockMvc.perform(post("/api/v1/templates/1/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("废弃模板 - 成功")
    void deprecateTemplate_Success() throws Exception {
        testTemplateDTO.setStatus(TemplateStatus.DEPRECATED);
        when(templateService.deprecateTemplate(anyLong())).thenReturn(testTemplateDTO);

        mockMvc.perform(post("/api/v1/templates/1/deprecate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEPRECATED"));
    }

    @Test
    @DisplayName("归档模板 - 成功")
    void archiveTemplate_Success() throws Exception {
        testTemplateDTO.setStatus(TemplateStatus.ARCHIVED);
        when(templateService.archiveTemplate(anyLong())).thenReturn(testTemplateDTO);

        mockMvc.perform(post("/api/v1/templates/1/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    @Test
    @DisplayName("获取版本历史")
    void getVersionHistory() throws Exception {
        TemplateVersionDTO versionDTO = TemplateVersionDTO.builder()
                .id(1L)
                .versionNumber("1.0.0")
                .changeDescription("Initial version")
                .build();

        when(versionService.getVersionHistory(anyLong(), anyInt(), anyInt())).thenReturn(List.of(versionDTO));

        mockMvc.perform(get("/api/v1/templates/1/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].versionNumber").value("1.0.0"));
    }

    @Test
    @DisplayName("验证数据")
    void validateData() throws Exception {
        when(templateService.getTemplateById(anyLong())).thenReturn(Optional.of(testTemplateDTO));
        when(templateService.validateFieldValues(anyString(), any())).thenReturn(Map.of());

        mockMvc.perform(post("/api/v1/templates/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"field1\":\"value1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    @DisplayName("获取版本统计")
    void getVersionStatistics() throws Exception {
        when(versionService.getVersionStatistics(anyLong())).thenReturn(Map.of("totalVersions", 5L));

        mockMvc.perform(get("/api/v1/templates/1/versions/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVersions").value(5));
    }
}
