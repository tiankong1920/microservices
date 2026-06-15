package com.inventory.templateservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.common.template.BusinessDomain;
import com.inventory.common.template.FieldPermission;
import com.inventory.common.template.TemplateStatus;
import com.inventory.common.template.constant.TemplateConstants;
import com.inventory.common.template.dto.CustomFieldDTO;
import com.inventory.common.template.dto.TemplateDTO;
import com.inventory.common.template.dto.TemplateFieldDTO;
import com.inventory.common.template.exception.TemplateException;
import com.inventory.common.template.service.ITemplateService;
import com.inventory.templateservice.entity.CustomField;
import com.inventory.templateservice.entity.Template;
import com.inventory.templateservice.entity.TemplateField;
import com.inventory.templateservice.repository.ICustomFieldRepository;
import com.inventory.templateservice.repository.ITemplateFieldRepository;
import com.inventory.templateservice.repository.ITemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateService implements ITemplateService {

    private final ITemplateRepository templateRepository;
    private final ITemplateFieldRepository templateFieldRepository;
    private final ICustomFieldRepository customFieldRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final TemplateVersionService templateVersionService;

    @Override
    @Transactional
    @CacheEvict(value = "templates", allEntries = true)
    public TemplateDTO createTemplate(TemplateDTO templateDTO) {
        log.info("Creating template: {}", templateDTO.getTemplateCode());

        if (templateRepository.existsByTemplateCodeAndTenantId(
                templateDTO.getTemplateCode(), templateDTO.getTenantId())) {
            throw TemplateException.alreadyExists(templateDTO.getTemplateCode());
        }

        Template template = modelMapper.map(templateDTO, Template.class);
        template.setStatus(TemplateStatus.DRAFT);
        template.setVersion("1.0.0");

        template = templateRepository.save(template);

        if (templateDTO.getFields() != null && !templateDTO.getFields().isEmpty()) {
            createTemplateFields(template, templateDTO.getFields());
        }

        templateVersionService.createVersion(template.getId(), "Initial version", "major");

        log.info("Template created successfully: {}", template.getTemplateCode());
        return toDTO(template);
    }

    @Override
    @Transactional
    @CacheEvict(value = "templates", key = "#id")
    public TemplateDTO updateTemplate(Long id, TemplateDTO templateDTO) {
        log.info("Updating template: {}", id);

        Template template = templateRepository.findById(id)
                .orElseThrow(() -> TemplateException.notFound(id));

        if (template.getStatus() == TemplateStatus.PUBLISHED) {
            throw TemplateException.cannotModify(id, "Published template cannot be modified directly");
        }

        String oldSnapshot = captureSnapshot(template);

        template.setName(templateDTO.getTemplateName());
        template.setDescription(templateDTO.getDescription());
        template.setCategory(templateDTO.getCategory());
        template.setDefaultValues(toJson(templateDTO.getDefaultValues()));
        template.setApplicableScenarios(toJson(templateDTO.getApplicableScenarios()));
        template.setAllowCustomFields(templateDTO.getAllowCustomFields());
        template.setMetadata(toJson(templateDTO.getMetadata()));
        template.setChangeLog(templateDTO.getChangeLog());

        template = templateRepository.save(template);

        String newSnapshot = captureSnapshot(template);
        if (!oldSnapshot.equals(newSnapshot)) {
            templateVersionService.createVersion(template.getId(), templateDTO.getChangeLog(), "minor");
        }

        log.info("Template updated successfully: {}", id);
        return toDTO(template);
    }

    @Override
    @Cacheable(value = "templates", key = "#id")
    public Optional<TemplateDTO> getTemplateById(Long id) {
        return templateRepository.findById(id).map(this::toDTO);
    }

    @Override
    @Cacheable(value = "templates", key = "'code:' + #templateCode")
    public Optional<TemplateDTO> getTemplateByCode(String templateCode) {
        return templateRepository.findByTemplateCode(templateCode).map(this::toDTO);
    }

    @Override
    public List<TemplateDTO> getTemplatesByDomain(String domain) {
        BusinessDomain businessDomain = BusinessDomain.valueOf(domain);
        return templateRepository.findByBusinessDomain(businessDomain).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TemplateDTO> getTemplatesByCategory(String category) {
        return templateRepository.findByCategory(category).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TemplateDTO> getTemplatesByStatus(String status) {
        TemplateStatus templateStatus = TemplateStatus.valueOf(status);
        return templateRepository.findByStatus(templateStatus).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TemplateDTO> searchTemplates(String keyword, String domain, String status, int page, int size) {
        BusinessDomain businessDomain = domain != null ? BusinessDomain.valueOf(domain) : null;
        TemplateStatus templateStatus = status != null ? TemplateStatus.valueOf(status) : null;
        
        Pageable pageable = PageRequest.of(page, Math.min(size, TemplateConstants.MAX_PAGE_SIZE));
        Page<Template> templates = templateRepository.search(
                TemplateConstants.DEFAULT_TENANT_ID, keyword, businessDomain, templateStatus, pageable);
        
        return templates.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "templates", key = "#id")
    public void deleteTemplate(Long id) {
        log.info("Deleting template: {}", id);

        Template template = templateRepository.findById(id)
                .orElseThrow(() -> TemplateException.notFound(id));

        if (template.getStatus() == TemplateStatus.PUBLISHED) {
            template.setStatus(TemplateStatus.ARCHIVED);
            templateRepository.save(template);
        } else {
            templateFieldRepository.deleteByTemplate_Id(id);
            customFieldRepository.deleteByTemplate_Id(id);
            templateRepository.deleteById(id);
        }

        log.info("Template deleted: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "templates", key = "#id")
    public TemplateDTO publishTemplate(Long id) {
        log.info("Publishing template: {}", id);

        Template template = templateRepository.findById(id)
                .orElseThrow(() -> TemplateException.notFound(id));

        if (template.getStatus() != TemplateStatus.APPROVED) {
            throw TemplateException.cannotModify(id, "Only approved template can be published");
        }

        template.setStatus(TemplateStatus.PUBLISHED);
        template = templateRepository.save(template);

        templateVersionService.createVersion(template.getId(), "Template published", "minor");

        log.info("Template published: {}", id);
        return toDTO(template);
    }

    @Override
    @Transactional
    @CacheEvict(value = "templates", key = "#id")
    public TemplateDTO deprecateTemplate(Long id) {
        log.info("Deprecating template: {}", id);

        Template template = templateRepository.findById(id)
                .orElseThrow(() -> TemplateException.notFound(id));

        template.setStatus(TemplateStatus.DEPRECATED);
        template = templateRepository.save(template);

        log.info("Template deprecated: {}", id);
        return toDTO(template);
    }

    @Override
    @Transactional
    @CacheEvict(value = "templates", key = "#id")
    public TemplateDTO archiveTemplate(Long id) {
        log.info("Archiving template: {}", id);

        Template template = templateRepository.findById(id)
                .orElseThrow(() -> TemplateException.notFound(id));

        template.setStatus(TemplateStatus.ARCHIVED);
        template = templateRepository.save(template);

        log.info("Template archived: {}", id);
        return toDTO(template);
    }

    @Override
    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public TemplateDTO addField(Long templateId, TemplateFieldDTO fieldDTO) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> TemplateException.notFound(templateId));

        if (template.getStatus() == TemplateStatus.PUBLISHED) {
            throw TemplateException.cannotModify(templateId, "Cannot add field to published template");
        }

        long fieldCount = templateFieldRepository.countByTemplate_Id(templateId);
        if (fieldCount >= TemplateConstants.MAX_FIELDS_PER_TEMPLATE) {
            throw TemplateException.customFieldLimitExceeded(templateId, TemplateConstants.MAX_FIELDS_PER_TEMPLATE);
        }

        if (templateFieldRepository.existsByTemplate_IdAndFieldCode(templateId, fieldDTO.getFieldCode())) {
            throw TemplateException.validationFailed(fieldDTO.getFieldCode(), "Field code already exists");
        }

        TemplateField field = modelMapper.map(fieldDTO, TemplateField.class);
        field.setTemplate(template);

        if (field.getDisplayOrder() == null) {
            Integer maxOrder = templateFieldRepository.findMaxDisplayOrder(templateId);
            field.setDisplayOrder(maxOrder == null ? 1 : maxOrder + 1);
        }

        templateFieldRepository.save(field);
        templateVersionService.createVersion(templateId, "Added field: " + fieldDTO.getFieldCode(), "patch");

        return toDTO(template);
    }

    @Override
    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public TemplateDTO updateField(Long templateId, Long fieldId, TemplateFieldDTO fieldDTO) {
        TemplateField field = templateFieldRepository.findById(fieldId)
                .orElseThrow(() -> TemplateException.fieldNotFound(fieldId));

        if (!field.getTemplate().getId().equals(templateId)) {
            throw TemplateException.fieldNotFound(fieldId);
        }

        modelMapper.map(fieldDTO, field);
        templateFieldRepository.save(field);
        templateVersionService.createVersion(templateId, "Updated field: " + fieldDTO.getFieldCode(), "patch");

        return getTemplateById(templateId).orElseThrow(() -> TemplateException.notFound(templateId));
    }

    @Override
    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public TemplateDTO removeField(Long templateId, Long fieldId) {
        TemplateField field = templateFieldRepository.findById(fieldId)
                .orElseThrow(() -> TemplateException.fieldNotFound(fieldId));

        if (!field.getTemplate().getId().equals(templateId)) {
            throw TemplateException.fieldNotFound(fieldId);
        }

        templateFieldRepository.deleteById(fieldId);
        templateVersionService.createVersion(templateId, "Removed field: " + field.getFieldCode(), "patch");

        return getTemplateById(templateId).orElseThrow(() -> TemplateException.notFound(templateId));
    }

    @Override
    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public TemplateDTO addCustomField(Long templateId, CustomFieldDTO customFieldDTO) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> TemplateException.notFound(templateId));

        if (!Boolean.TRUE.equals(template.getAllowCustomFields())) {
            throw TemplateException.cannotModify(templateId, "Template does not allow custom fields");
        }

        long customFieldCount = customFieldRepository.countActiveByTemplateId(templateId);
        if (customFieldCount >= TemplateConstants.MAX_CUSTOM_FIELDS_PER_TEMPLATE) {
            throw TemplateException.customFieldLimitExceeded(templateId, TemplateConstants.MAX_CUSTOM_FIELDS_PER_TEMPLATE);
        }

        CustomField customField = modelMapper.map(customFieldDTO, CustomField.class);
        customField.setTemplate(template);
        customField.setActive(true);
        customFieldRepository.save(customField);

        templateVersionService.createVersion(templateId, "Added custom field: " + customFieldDTO.getFieldCode(), "minor");

        return toDTO(template);
    }

    @Override
    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public TemplateDTO removeCustomField(Long templateId, Long customFieldId) {
        CustomField customField = customFieldRepository.findById(customFieldId)
                .orElseThrow(() -> TemplateException.fieldNotFound(customFieldId));

        if (!customField.getTemplate().getId().equals(templateId)) {
            throw TemplateException.fieldNotFound(customFieldId);
        }

        customField.setActive(false);
        customFieldRepository.save(customField);

        templateVersionService.createVersion(templateId, "Deactivated custom field: " + customField.getFieldCode(), "minor");

        return getTemplateById(templateId).orElseThrow(() -> TemplateException.notFound(templateId));
    }

    @Override
    public boolean validateTemplateData(String templateCode, Map<String, Object> data) {
        TemplateDTO template = getTemplateByCode(templateCode)
                .orElseThrow(() -> TemplateException.notFoundByCode(templateCode));

        Map<String, String> errors = validateFieldValues(templateCode, data);
        return errors.isEmpty();
    }

    @Override
    public Map<String, String> validateFieldValues(String templateCode, Map<String, Object> data) {
        Map<String, String> errors = new HashMap<>();

        Template template = templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> TemplateException.notFoundByCode(templateCode));

        List<TemplateField> fields = templateFieldRepository.findByTemplate_Id(template.getId());

        for (TemplateField field : fields) {
            Object value = data.get(field.getFieldCode());

            if (Boolean.TRUE.equals(field.getRequired()) && (value == null || value.toString().isEmpty())) {
                errors.put(field.getFieldCode(), "Field is required");
                continue;
            }

            if (value != null) {
                String error = validateFieldValue(field, value);
                if (error != null) {
                    errors.put(field.getFieldCode(), error);
                }
            }
        }

        return errors;
    }

    @Override
    public List<TemplateFieldDTO> getRequiredFields(String templateCode) {
        Template template = templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> TemplateException.notFoundByCode(templateCode));

        return templateFieldRepository.findByTemplate_IdAndRequiredTrue(template.getId()).stream()
                .map(f -> modelMapper.map(f, TemplateFieldDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<TemplateFieldDTO> getFieldsByPermission(String templateCode, String permission) {
        Template template = templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> TemplateException.notFoundByCode(templateCode));

        FieldPermission fieldPermission = FieldPermission.valueOf(permission);

        return templateFieldRepository.findByTemplate_Id(template.getId()).stream()
                .filter(f -> f.getDefaultPermission() == fieldPermission)
                .map(f -> modelMapper.map(f, TemplateFieldDTO.class))
                .collect(Collectors.toList());
    }

    private void createTemplateFields(Template template, List<TemplateFieldDTO> fieldDTOs) {
        int order = 1;
        for (TemplateFieldDTO fieldDTO : fieldDTOs) {
            TemplateField field = modelMapper.map(fieldDTO, TemplateField.class);
            field.setTemplate(template);
            if (field.getDisplayOrder() == null) {
                field.setDisplayOrder(order++);
            }
            templateFieldRepository.save(field);
        }
    }

    private String validateFieldValue(TemplateField field, Object value) {
        String strValue = value.toString();

        if (field.getMinLength() != null && strValue.length() < field.getMinLength()) {
            return "Minimum length is " + field.getMinLength();
        }

        if (field.getMaxLength() != null && strValue.length() > field.getMaxLength()) {
            return "Maximum length is " + field.getMaxLength();
        }

        if (field.getValidationRegex() != null && !field.getValidationRegex().isEmpty()) {
            if (!strValue.matches(field.getValidationRegex())) {
                return "Value does not match required format";
            }
        }

        return null;
    }

    private String captureSnapshot(Template template) {
        try {
            return objectMapper.writeValueAsString(toDTO(template));
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Map<String, Object> toMap(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private java.util.Set<String> toSet(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.Set<String>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private TemplateDTO toDTO(Template template) {
        TemplateDTO dto = modelMapper.map(template, TemplateDTO.class);
        dto.setTemplateName(template.getName());

        List<TemplateField> fields = templateFieldRepository.findByTemplate_IdOrderByDisplayOrderAsc(template.getId());
        dto.setFields(fields.stream()
                .map(f -> modelMapper.map(f, TemplateFieldDTO.class))
                .collect(Collectors.toList()));

        List<CustomField> customFields = customFieldRepository.findByTemplate_IdAndActiveTrue(template.getId());
        dto.setDefaultValues(toMap(template.getDefaultValues()));
        dto.setApplicableScenarios(toSet(template.getApplicableScenarios()));
        dto.setMetadata(template.getMetadata() != null ? toMap(template.getMetadata()) : Map.of("customFieldCount", customFields.size()));

        return dto;
    }

    @Override
    @Transactional
    public TemplateDTO copyTemplate(Long sourceId, String newTemplateCode) {
        log.info("Copying template {} to new code {}", sourceId, newTemplateCode);

        Template source = templateRepository.findById(sourceId)
                .orElseThrow(() -> TemplateException.notFound(sourceId));

        if (templateRepository.existsByTemplateCode(newTemplateCode)) {
            throw TemplateException.alreadyExists(newTemplateCode);
        }

        Template copy = Template.builder()
                .templateCode(newTemplateCode)
                .name(source.getName() + " (Copy)")
                .description(source.getDescription())
                .businessDomain(source.getBusinessDomain())
                .category(source.getCategory())
                .status(TemplateStatus.DRAFT)
                .version("1.0.0")
                .defaultValues(source.getDefaultValues())
                .applicableScenarios(source.getApplicableScenarios())
                .allowCustomFields(source.getAllowCustomFields())
                .allowExtension(source.getAllowExtension())
                .extensionPoint(source.getExtensionPoint())
                .metadata(source.getMetadata())
                .tenantId(source.getTenantId())
                .build();

        copy = templateRepository.save(copy);

        List<TemplateField> sourceFields = templateFieldRepository.findByTemplate_IdOrderByDisplayOrderAsc(sourceId);
        for (TemplateField sourceField : sourceFields) {
            TemplateField fieldCopy = TemplateField.builder()
                    .template(copy)
                    .fieldCode(sourceField.getFieldCode())
                    .fieldName(sourceField.getFieldName())
                    .fieldLabel(sourceField.getFieldLabel())
                    .fieldType(sourceField.getFieldType())
                    .description(sourceField.getDescription())
                    .placeholder(sourceField.getPlaceholder())
                    .defaultValue(sourceField.getDefaultValue())
                    .required(sourceField.getRequired())
                    .unique(sourceField.getUnique())
                    .searchable(sourceField.getSearchable())
                    .sortable(sourceField.getSortable())
                    .displayOrder(sourceField.getDisplayOrder())
                    .validationRegex(sourceField.getValidationRegex())
                    .minLength(sourceField.getMinLength())
                    .maxLength(sourceField.getMaxLength())
                    .minValue(sourceField.getMinValue())
                    .maxValue(sourceField.getMaxValue())
                    .options(sourceField.getOptions())
                    .extraConfig(sourceField.getExtraConfig())
                    .defaultPermission(sourceField.getDefaultPermission())
                    .sensitive(sourceField.getSensitive())
                    .build();
            templateFieldRepository.save(fieldCopy);
        }

        log.info("Template copied successfully: {} -> {}", sourceId, copy.getId());
        return toDTO(copy);
    }
}
