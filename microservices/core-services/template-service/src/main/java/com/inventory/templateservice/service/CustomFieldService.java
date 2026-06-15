package com.inventory.templateservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.common.template.FieldPermission;
import com.inventory.common.template.FieldType;
import com.inventory.common.template.constant.TemplateConstants;
import com.inventory.common.template.dto.CustomFieldDTO;
import com.inventory.common.template.dto.ValidationRuleDTO;
import com.inventory.common.template.exception.TemplateException;
import com.inventory.common.template.service.ICustomFieldService;
import com.inventory.templateservice.entity.CustomField;
import com.inventory.templateservice.entity.Template;
import com.inventory.templateservice.entity.ValidationRule;
import com.inventory.templateservice.repository.ICustomFieldRepository;
import com.inventory.templateservice.repository.ITemplateRepository;
import com.inventory.templateservice.repository.IValidationRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomFieldService implements ICustomFieldService {

    private final ICustomFieldRepository customFieldRepository;
    private final ITemplateRepository templateRepository;
    private final IValidationRuleRepository validationRuleRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    @CacheEvict(value = "customFields", key = "#templateId")
    public CustomFieldDTO createCustomField(Long templateId, CustomFieldDTO customFieldDTO) {
        log.info("Creating custom field for template: {}", templateId);

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> TemplateException.notFound(templateId));

        if (!Boolean.TRUE.equals(template.getAllowCustomFields())) {
            throw TemplateException.cannotModify(templateId, "Template does not allow custom fields");
        }

        long count = customFieldRepository.countActiveByTemplateId(templateId);
        if (count >= TemplateConstants.MAX_CUSTOM_FIELDS_PER_TEMPLATE) {
            throw TemplateException.customFieldLimitExceeded(templateId, TemplateConstants.MAX_CUSTOM_FIELDS_PER_TEMPLATE);
        }

        if (customFieldRepository.existsByTemplate_IdAndFieldCode(templateId, customFieldDTO.getFieldCode())) {
            throw TemplateException.validationFailed(customFieldDTO.getFieldCode(), "Field code already exists");
        }

        CustomField customField = modelMapper.map(customFieldDTO, CustomField.class);
        customField.setTemplate(template);
        customField.setActive(true);
        customField.setOptions(toJson(customFieldDTO.getOptions()));
        customField.setRolePermissions(toJson(customFieldDTO.getRolePermissions()));

        customField = customFieldRepository.save(customField);

        log.info("Custom field created: {} for template: {}", customField.getFieldCode(), templateId);
        return toCustomFieldDTO(customField);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customFields", key = "#id")
    public CustomFieldDTO updateCustomField(Long id, CustomFieldDTO customFieldDTO) {
        log.info("Updating custom field: {}", id);

        CustomField customField = customFieldRepository.findById(id)
                .orElseThrow(() -> TemplateException.fieldNotFound(id));

        customField.setFieldName(customFieldDTO.getFieldName());
        customField.setFieldLabel(customFieldDTO.getFieldLabel());
        customField.setDescription(customFieldDTO.getDescription());
        customField.setDefaultValue(customFieldDTO.getDefaultValue());
        customField.setRequired(customFieldDTO.getRequired());
        customField.setValidationRegex(customFieldDTO.getValidationRegex());
        customField.setMinLength(customFieldDTO.getMinLength());
        customField.setMaxLength(customFieldDTO.getMaxLength());
        customField.setMinValue(customFieldDTO.getMinValue());
        customField.setMaxValue(customFieldDTO.getMaxValue());
        customField.setOptions(toJson(customFieldDTO.getOptions()));
        customField.setRolePermissions(toJson(customFieldDTO.getRolePermissions()));

        customField = customFieldRepository.save(customField);

        log.info("Custom field updated: {}", id);
        return toCustomFieldDTO(customField);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customFields", allEntries = true)
    public void deleteCustomField(Long id) {
        log.info("Deleting custom field: {}", id);

        CustomField customField = customFieldRepository.findById(id)
                .orElseThrow(() -> TemplateException.fieldNotFound(id));

        customField.setActive(false);
        customFieldRepository.save(customField);

        log.info("Custom field deactivated: {}", id);
    }

    @Override
    @Transactional
    public void activateCustomField(Long id) {
        log.info("Activating custom field: {}", id);

        CustomField customField = customFieldRepository.findById(id)
                .orElseThrow(() -> TemplateException.fieldNotFound(id));

        customField.setActive(true);
        customFieldRepository.save(customField);

        log.info("Custom field activated: {}", id);
    }

    @Override
    @Transactional
    public void deactivateCustomField(Long id) {
        log.info("Deactivating custom field: {}", id);

        CustomField customField = customFieldRepository.findById(id)
                .orElseThrow(() -> TemplateException.fieldNotFound(id));

        customField.setActive(false);
        customFieldRepository.save(customField);

        log.info("Custom field deactivated: {}", id);
    }

    @Override
    @Cacheable(value = "customFields", key = "#id")
    public Optional<CustomFieldDTO> getCustomFieldById(Long id) {
        return customFieldRepository.findById(id)
                .map(this::toCustomFieldDTO);
    }

    @Override
    public List<CustomFieldDTO> getCustomFieldsByTemplate(Long templateId) {
        return customFieldRepository.findByTemplate_Id(templateId).stream()
                .map(this::toCustomFieldDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomFieldDTO> getActiveCustomFields(Long templateId) {
        return customFieldRepository.findByTemplate_IdAndActiveTrue(templateId).stream()
                .map(this::toCustomFieldDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ValidationRuleDTO createValidationRule(ValidationRuleDTO ruleDTO) {
        log.info("Creating validation rule: {}", ruleDTO.getRuleCode());

        if (validationRuleRepository.existsByRuleCode(ruleDTO.getRuleCode())) {
            throw TemplateException.validationFailed(ruleDTO.getRuleCode(), "Rule code already exists");
        }

        if (ruleDTO.getRegexPattern() != null && !isValidRegex(ruleDTO.getRegexPattern())) {
            throw TemplateException.validationFailed(ruleDTO.getRuleCode(), "Invalid regex pattern");
        }

        ValidationRule rule = modelMapper.map(ruleDTO, ValidationRule.class);
        rule.setBuiltIn(false);
        rule.setActive(true);
        rule.setParameters(toJson(ruleDTO.getParameters()));
        rule.setApplicableFieldTypes(toJson(ruleDTO.getApplicableFieldTypes()));

        rule = validationRuleRepository.save(rule);

        log.info("Validation rule created: {}", rule.getRuleCode());
        return toRuleDTO(rule);
    }

    @Override
    @Transactional
    public ValidationRuleDTO updateValidationRule(Long id, ValidationRuleDTO ruleDTO) {
        log.info("Updating validation rule: {}", id);

        ValidationRule rule = validationRuleRepository.findById(id)
                .orElseThrow(() -> TemplateException.validationRuleNotFound(id));

        if (Boolean.TRUE.equals(rule.getBuiltIn())) {
            throw TemplateException.cannotModify(id, "Built-in validation rule cannot be modified");
        }

        if (ruleDTO.getRegexPattern() != null && !isValidRegex(ruleDTO.getRegexPattern())) {
            throw TemplateException.validationFailed(ruleDTO.getRuleCode(), "Invalid regex pattern");
        }

        rule.setRuleName(ruleDTO.getRuleName());
        rule.setRuleType(ruleDTO.getRuleType());
        rule.setRegexPattern(ruleDTO.getRegexPattern());
        rule.setErrorMessage(ruleDTO.getErrorMessage());
        rule.setParameters(toJson(ruleDTO.getParameters()));
        rule.setApplicableFieldTypes(toJson(ruleDTO.getApplicableFieldTypes()));

        rule = validationRuleRepository.save(rule);

        log.info("Validation rule updated: {}", id);
        return toRuleDTO(rule);
    }

    @Override
    @Transactional
    public void deleteValidationRule(Long id) {
        log.info("Deleting validation rule: {}", id);

        ValidationRule rule = validationRuleRepository.findById(id)
                .orElseThrow(() -> TemplateException.validationRuleNotFound(id));

        if (Boolean.TRUE.equals(rule.getBuiltIn())) {
            throw TemplateException.cannotModify(id, "Built-in validation rule cannot be deleted");
        }

        rule.setActive(false);
        validationRuleRepository.save(rule);

        log.info("Validation rule deactivated: {}", id);
    }

    @Override
    public Optional<ValidationRuleDTO> getValidationRuleById(Long id) {
        return validationRuleRepository.findById(id)
                .map(this::toRuleDTO);
    }

    @Override
    public Optional<ValidationRuleDTO> getValidationRuleByCode(String ruleCode) {
        return validationRuleRepository.findByRuleCode(ruleCode)
                .map(this::toRuleDTO);
    }

    @Override
    public List<ValidationRuleDTO> getAllValidationRules() {
        return validationRuleRepository.findByActiveTrue().stream()
                .map(this::toRuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ValidationRuleDTO> getBuiltInValidationRules() {
        return validationRuleRepository.findByBuiltInTrueAndActiveTrue().stream()
                .map(this::toRuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ValidationRuleDTO> getValidationRulesByFieldType(FieldType fieldType) {
        return validationRuleRepository.findByApplicableFieldType(fieldType.name()).stream()
                .map(this::toRuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> validateFieldValue(Long customFieldId, Object value) {
        Map<String, String> errors = new LinkedHashMap<>();

        CustomField customField = customFieldRepository.findById(customFieldId)
                .orElseThrow(() -> TemplateException.fieldNotFound(customFieldId));

        if (value == null) {
            if (Boolean.TRUE.equals(customField.getRequired())) {
                errors.put("required", "Field is required");
            }
            return errors;
        }

        String strValue = value.toString();

        if (customField.getMinLength() != null && strValue.length() < customField.getMinLength()) {
            errors.put("minLength", "Minimum length is " + customField.getMinLength());
        }

        if (customField.getMaxLength() != null && strValue.length() > customField.getMaxLength()) {
            errors.put("maxLength", "Maximum length is " + customField.getMaxLength());
        }

        if (customField.getValidationRegex() != null && !customField.getValidationRegex().isEmpty()) {
            if (!strValue.matches(customField.getValidationRegex())) {
                errors.put("pattern", customField.getDescription() != null ?
                        customField.getDescription() : "Value does not match required format");
            }
        }

        if (customField.getFieldType() == FieldType.NUMBER) {
            try {
                double numValue = Double.parseDouble(strValue);
                if (customField.getMinValue() != null && numValue < customField.getMinValue()) {
                    errors.put("minValue", "Minimum value is " + customField.getMinValue());
                }
                if (customField.getMaxValue() != null && numValue > customField.getMaxValue()) {
                    errors.put("maxValue", "Maximum value is " + customField.getMaxValue());
                }
            } catch (NumberFormatException e) {
                errors.put("type", "Value must be a number");
            }
        }

        if (customField.getOptions() != null && !customField.getOptions().isEmpty()) {
            if (!customField.getOptions().contains(strValue)) {
                errors.put("options", "Value must be one of: " + String.join(", ", customField.getOptions()));
            }
        }

        return errors;
    }

    @Override
    public boolean isValidRegex(String regexPattern) {
        try {
            Pattern.compile(regexPattern);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<String> parseJsonToList(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private ValidationRuleDTO toRuleDTO(ValidationRule rule) {
        ValidationRuleDTO dto = modelMapper.map(rule, ValidationRuleDTO.class);
        dto.setParameters(parseJsonToMap(rule.getParameters()));
        dto.setApplicableFieldTypes(parseJsonToList(rule.getApplicableFieldTypes()));
        return dto;
    }

    private CustomFieldDTO toCustomFieldDTO(CustomField field) {
        CustomFieldDTO dto = modelMapper.map(field, CustomFieldDTO.class);
        dto.setOptions(parseJsonToList(field.getOptions()));
        dto.setRolePermissions(parseJsonToFieldPermissionMap(field.getRolePermissions()));
        return dto;
    }

    @SuppressWarnings("unchecked")
    private Map<String, FieldPermission> parseJsonToFieldPermissionMap(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            Map<String, String> map = objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
            Map<String, FieldPermission> result = new LinkedHashMap<>();
            if (map != null) {
                map.forEach((key, value) -> {
                    try {
                        result.put(key, FieldPermission.valueOf(value));
                    } catch (IllegalArgumentException ignored) {
                    }
                });
            }
            return result;
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
