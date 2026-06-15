package com.inventory.templateservice.service;

import com.inventory.common.template.BusinessDomain;
import com.inventory.common.template.TemplateStatus;
import com.inventory.common.template.dto.TemplateDTO;
import com.inventory.common.template.exception.TemplateException;
import com.inventory.templateservice.entity.Template;
import com.inventory.templateservice.repository.ICustomFieldRepository;
import com.inventory.templateservice.repository.ITemplateFieldRepository;
import com.inventory.templateservice.repository.ITemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private ITemplateRepository templateRepository;

    @Mock
    private ITemplateFieldRepository templateFieldRepository;

    @Mock
    private ICustomFieldRepository customFieldRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TemplateVersionService templateVersionService;

    private TemplateService templateService;

    private Template testTemplate;
    private TemplateDTO testTemplateDTO;

    @BeforeEach
    void setUp() {
        testTemplate = Template.builder()
                .id(1L)
                .templateCode("TPL_TEST_001")
                .name("Test Template")
                .description("Test Description")
                .businessDomain(BusinessDomain.CUSTOMER_MANAGEMENT)
                .status(TemplateStatus.DRAFT)
                .version("1.0.0")
                .tenantId("default")
                .allowCustomFields(true)
                .build();

        testTemplateDTO = TemplateDTO.builder()
                .id(1L)
                .templateCode("TPL_TEST_001")
                .templateName("Test Template")
                .description("Test Description")
                .businessDomain(BusinessDomain.CUSTOMER_MANAGEMENT)
                .status(TemplateStatus.DRAFT)
                .version("1.0.0")
                .tenantId("default")
                .allowCustomFields(true)
                .build();

        templateService = new TemplateService(
                templateRepository,
                templateFieldRepository,
                customFieldRepository,
                modelMapper,
                objectMapper,
                templateVersionService
        );
    }

    @Test
    @DisplayName("创建模板 - 成功")
    void createTemplate_Success() {
        when(templateRepository.existsByTemplateCodeAndTenantId(anyString(), anyString())).thenReturn(false);
        when(modelMapper.map(any(TemplateDTO.class), eq(Template.class))).thenReturn(testTemplate);
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);
        when(modelMapper.map(any(Template.class), eq(TemplateDTO.class))).thenReturn(testTemplateDTO);
        when(templateFieldRepository.findByTemplate_IdOrderByDisplayOrderAsc(anyLong())).thenReturn(List.of());
        when(customFieldRepository.findByTemplate_IdAndActiveTrue(anyLong())).thenReturn(List.of());

        TemplateDTO result = templateService.createTemplate(testTemplateDTO);

        assertNotNull(result);
        assertEquals("TPL_TEST_001", result.getTemplateCode());
        verify(templateRepository).save(any(Template.class));
        verify(templateVersionService).createVersion(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("创建模板 - 模板编码已存在")
    void createTemplate_AlreadyExists() {
        when(templateRepository.existsByTemplateCodeAndTenantId(anyString(), anyString())).thenReturn(true);

        assertThrows(TemplateException.class, () -> templateService.createTemplate(testTemplateDTO));
        verify(templateRepository, never()).save(any(Template.class));
    }

    @Test
    @DisplayName("获取模板 - 成功")
    void getTemplateById_Success() {
        when(templateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));
        when(modelMapper.map(any(Template.class), eq(TemplateDTO.class))).thenReturn(testTemplateDTO);
        when(templateFieldRepository.findByTemplate_IdOrderByDisplayOrderAsc(anyLong())).thenReturn(List.of());
        when(customFieldRepository.findByTemplate_IdAndActiveTrue(anyLong())).thenReturn(List.of());

        Optional<TemplateDTO> result = templateService.getTemplateById(1L);

        assertTrue(result.isPresent());
        assertEquals("TPL_TEST_001", result.get().getTemplateCode());
    }

    @Test
    @DisplayName("获取模板 - 不存在")
    void getTemplateById_NotFound() {
        when(templateRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<TemplateDTO> result = templateService.getTemplateById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("更新模板 - 成功")
    void updateTemplate_Success() throws Exception {
        Template existingTemplate = Template.builder()
                .id(1L)
                .templateCode("TPL_TEST_001")
                .name("Old Name")
                .status(TemplateStatus.DRAFT)
                .build();

        when(templateRepository.findById(anyLong())).thenReturn(Optional.of(existingTemplate));
        when(templateRepository.save(any(Template.class))).thenReturn(existingTemplate);
        when(modelMapper.map(any(Template.class), eq(TemplateDTO.class))).thenReturn(testTemplateDTO);
        when(templateFieldRepository.findByTemplate_IdOrderByDisplayOrderAsc(anyLong())).thenReturn(List.of());
        when(customFieldRepository.findByTemplate_IdAndActiveTrue(anyLong())).thenReturn(List.of());
        doThrow(new com.fasterxml.jackson.core.JsonProcessingException("test") {})
                .when(objectMapper).writeValueAsString(any());

        TemplateDTO result = templateService.updateTemplate(1L, testTemplateDTO);

        assertNotNull(result);
        verify(templateRepository).save(any(Template.class));
    }

    @Test
    @DisplayName("更新模板 - 已发布模板不可修改")
    void updateTemplate_CannotModifyPublished() {
        Template publishedTemplate = Template.builder()
                .id(1L)
                .templateCode("TPL_TEST_001")
                .status(TemplateStatus.PUBLISHED)
                .build();

        when(templateRepository.findById(anyLong())).thenReturn(Optional.of(publishedTemplate));

        assertThrows(TemplateException.class, () -> templateService.updateTemplate(1L, testTemplateDTO));
    }

    @Test
    @DisplayName("删除模板 - 草稿状态直接删除")
    void deleteTemplate_Draft() {
        when(templateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));
        doNothing().when(templateFieldRepository).deleteByTemplate_Id(anyLong());
        doNothing().when(customFieldRepository).deleteByTemplate_Id(anyLong());
        doNothing().when(templateRepository).deleteById(anyLong());

        templateService.deleteTemplate(1L);

        verify(templateRepository).deleteById(anyLong());
    }

    @Test
    @DisplayName("删除模板 - 已发布状态归档")
    void deleteTemplate_Published_Archive() {
        testTemplate.setStatus(TemplateStatus.PUBLISHED);
        when(templateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);

        templateService.deleteTemplate(1L);

        verify(templateRepository).save(any(Template.class));
        verify(templateRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("发布模板 - 成功")
    void publishTemplate_Success() {
        testTemplate.setStatus(TemplateStatus.APPROVED);
        when(templateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);
        when(modelMapper.map(any(Template.class), eq(TemplateDTO.class))).thenReturn(testTemplateDTO);
        when(templateFieldRepository.findByTemplate_IdOrderByDisplayOrderAsc(anyLong())).thenReturn(List.of());
        when(customFieldRepository.findByTemplate_IdAndActiveTrue(anyLong())).thenReturn(List.of());

        TemplateDTO result = templateService.publishTemplate(1L);

        assertNotNull(result);
        verify(templateRepository).save(any(Template.class));
    }

    @Test
    @DisplayName("发布模板 - 非审核通过状态不可发布")
    void publishTemplate_NotApproved() {
        testTemplate.setStatus(TemplateStatus.DRAFT);
        when(templateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));

        assertThrows(TemplateException.class, () -> templateService.publishTemplate(1L));
    }

    @Test
    @DisplayName("按领域获取模板")
    void getTemplatesByDomain() {
        List<Template> templates = List.of(testTemplate);
        when(templateRepository.findByBusinessDomain(any(BusinessDomain.class))).thenReturn(templates);
        when(modelMapper.map(any(Template.class), eq(TemplateDTO.class))).thenReturn(testTemplateDTO);
        when(templateFieldRepository.findByTemplate_IdOrderByDisplayOrderAsc(anyLong())).thenReturn(List.of());
        when(customFieldRepository.findByTemplate_IdAndActiveTrue(anyLong())).thenReturn(List.of());

        List<TemplateDTO> result = templateService.getTemplatesByDomain("CUSTOMER_MANAGEMENT");

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
