package com.inventory.common.template.service;

import com.inventory.common.template.dto.CustomFieldDTO;
import com.inventory.common.template.dto.ValidationRuleDTO;
import com.inventory.common.template.FieldType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ICustomFieldService {

    CustomFieldDTO createCustomField(Long templateId, CustomFieldDTO customField);

    CustomFieldDTO updateCustomField(Long id, CustomFieldDTO customField);

    Optional<CustomFieldDTO> getCustomFieldById(Long id);

    List<CustomFieldDTO> getCustomFieldsByTemplate(Long templateId);

    List<CustomFieldDTO> getActiveCustomFields(Long templateId);

    void deleteCustomField(Long id);

    void activateCustomField(Long id);

    void deactivateCustomField(Long id);

    ValidationRuleDTO createValidationRule(ValidationRuleDTO rule);

    ValidationRuleDTO updateValidationRule(Long id, ValidationRuleDTO rule);

    Optional<ValidationRuleDTO> getValidationRuleById(Long id);

    Optional<ValidationRuleDTO> getValidationRuleByCode(String ruleCode);

    List<ValidationRuleDTO> getAllValidationRules();

    List<ValidationRuleDTO> getBuiltInValidationRules();

    List<ValidationRuleDTO> getValidationRulesByFieldType(FieldType fieldType);

    void deleteValidationRule(Long id);

    Map<String, String> validateFieldValue(Long customFieldId, Object value);

    boolean isValidRegex(String regexPattern);
}
