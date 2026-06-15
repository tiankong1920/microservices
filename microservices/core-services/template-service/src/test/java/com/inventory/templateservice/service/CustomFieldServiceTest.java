package com.inventory.templateservice.service;

import com.inventory.common.template.dto.CustomFieldDTO;
import com.inventory.common.template.dto.ValidationRuleDTO;
import com.inventory.common.template.exception.TemplateException;
import com.inventory.templateservice.entity.CustomField;
import com.inventory.templateservice.entity.Template;
import com.inventory.templateservice.entity.ValidationRule;
import com.inventory.templateservice.repository.ICustomFieldRepository;
import com.inventory.templateservice.repository.ITemplateRepository;
import com.inventory.templateservice.repository.IValidationRuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomFieldServiceTest {

    @Mock
    private ICustomFieldRepository customFieldRepository;

    @Mock
    private ITemplateRepository templateRepository;

    @Mock
    private IValidationRuleRepository validationRuleRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ObjectMapper objectMapper;

    private CustomFieldService customFieldService;

    private Template testTemplate;
    private CustomField testCustomField;
    private CustomFieldDTO testCustomFieldDTO;
    private ValidationRule testValidationRule;
    private ValidationRuleDTO testValidationRuleDTO;

    @BeforeEach
    void setUp() {
        testTemplate = Template.builder()
                .id(1L)
                .templateCode("TPL_TEST_001")
                .name("Test Template")
                .allowCustomFields(true)
                .build();

        testCustomField = CustomField.builder()
                .id(1L)
                .template(testTemplate)
                .fieldCode("CUSTOM_FIELD_001")
                .fieldName("Custom Field")
                .fieldType(com.inventory.common.template.FieldType.TEXT)
                .required(false)
                .active(true)
                .build();

        testCustomFieldDTO = CustomFieldDTO.builder()
                .id(1L)
                .templateId(1L)
                .fieldCode("CUSTOM_FIELD_001")
                .fieldName("Custom Field")
                .fieldType(com.inventory.common.template.FieldType.TEXT)
                .required(false)
                .active(true)
                .build();

        testValidationRule = ValidationRule.builder()
                .id(1L)
                .ruleCode("EMAIL")
                .ruleName("Email Format")
                .ruleType("REGEX")
                .regexPattern("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
                .active(true)
                .build();

        testValidationRuleDTO = ValidationRuleDTO.builder()
                .id(1L)
                .ruleCode("EMAIL")
                .ruleName("Email Format")
                .ruleType("REGEX")
                .regexPattern("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
                .active(true)
                .build();

        customFieldService = new CustomFieldService(customFieldRepository, templateRepository, validationRuleRepository, modelMapper, objectMapper);
    }

    @Test
    @DisplayName("创建自定义字段 - 成功")
    void createCustomField_Success() {
        when(templateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));
        when(customFieldRepository.existsByTemplate_IdAndFieldCode(anyLong(), anyString())).thenReturn(false);
        when(customFieldRepository.save(any(CustomField.class))).thenReturn(testCustomField);
        when(modelMapper.map(any(CustomFieldDTO.class), eq(CustomField.class))).thenReturn(testCustomField);
        when(modelMapper.map(any(CustomField.class), eq(CustomFieldDTO.class))).thenReturn(testCustomFieldDTO);

        CustomFieldDTO result = customFieldService.createCustomField(1L, testCustomFieldDTO);

        assertNotNull(result);
        assertEquals("CUSTOM_FIELD_001", result.getFieldCode());
    }

    @Test
    @DisplayName("创建自定义字段 - 模板不允许自定义字段")
    void createCustomField_NotAllowed() {
        testTemplate.setAllowCustomFields(false);
        when(templateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));

        assertThrows(TemplateException.class, () -> customFieldService.createCustomField(1L, testCustomFieldDTO));
    }

    @Test
    @DisplayName("获取模板的自定义字段")
    void getCustomFieldsByTemplate() {
        when(customFieldRepository.findByTemplate_Id(anyLong())).thenReturn(List.of(testCustomField));
        when(modelMapper.map(any(CustomField.class), eq(CustomFieldDTO.class))).thenReturn(testCustomFieldDTO);

        List<CustomFieldDTO> result = customFieldService.getCustomFieldsByTemplate(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("创建校验规则 - 成功")
    void createValidationRule_Success() {
        when(validationRuleRepository.existsByRuleCode(anyString())).thenReturn(false);
        when(validationRuleRepository.save(any(ValidationRule.class))).thenReturn(testValidationRule);
        when(modelMapper.map(any(ValidationRuleDTO.class), eq(ValidationRule.class))).thenReturn(testValidationRule);
        when(modelMapper.map(any(ValidationRule.class), eq(ValidationRuleDTO.class))).thenReturn(testValidationRuleDTO);

        ValidationRuleDTO result = customFieldService.createValidationRule(testValidationRuleDTO);

        assertNotNull(result);
        assertEquals("EMAIL", result.getRuleCode());
    }

    @Test
    @DisplayName("创建校验规则 - 规则编码已存在")
    void createValidationRule_AlreadyExists() {
        when(validationRuleRepository.existsByRuleCode(anyString())).thenReturn(true);

        assertThrows(TemplateException.class, () -> customFieldService.createValidationRule(testValidationRuleDTO));
    }

    @Test
    @DisplayName("获取所有校验规则")
    void getAllValidationRules() {
        when(validationRuleRepository.findByActiveTrue()).thenReturn(List.of(testValidationRule));
        when(modelMapper.map(any(ValidationRule.class), eq(ValidationRuleDTO.class))).thenReturn(testValidationRuleDTO);

        List<ValidationRuleDTO> result = customFieldService.getAllValidationRules();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("验证字段值 - 空值且非必填")
    void validateFieldValue_NullNotRequired() {
        testCustomField.setRequired(false);
        when(customFieldRepository.findById(anyLong())).thenReturn(Optional.of(testCustomField));

        Map<String, String> errors = customFieldService.validateFieldValue(1L, null);

        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("验证字段值 - 空值且必填")
    void validateFieldValue_NullRequired() {
        testCustomField.setRequired(true);
        when(customFieldRepository.findById(anyLong())).thenReturn(Optional.of(testCustomField));

        Map<String, String> errors = customFieldService.validateFieldValue(1L, null);

        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("required"));
    }
}
