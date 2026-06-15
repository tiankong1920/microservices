package com.inventory.common.template.service;

import com.inventory.common.template.dto.CustomFieldDTO;
import com.inventory.common.template.dto.TemplateDTO;
import com.inventory.common.template.dto.TemplateFieldDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ITemplateService {

    TemplateDTO createTemplate(TemplateDTO template);

    TemplateDTO updateTemplate(Long id, TemplateDTO template);

    Optional<TemplateDTO> getTemplateById(Long id);

    Optional<TemplateDTO> getTemplateByCode(String templateCode);

    List<TemplateDTO> getTemplatesByDomain(String domain);

    List<TemplateDTO> getTemplatesByCategory(String category);

    List<TemplateDTO> getTemplatesByStatus(String status);

    List<TemplateDTO> searchTemplates(String keyword, String domain, String status, int page, int size);

    void deleteTemplate(Long id);

    TemplateDTO publishTemplate(Long id);

    TemplateDTO deprecateTemplate(Long id);

    TemplateDTO archiveTemplate(Long id);

    TemplateDTO addField(Long templateId, TemplateFieldDTO field);

    TemplateDTO updateField(Long templateId, Long fieldId, TemplateFieldDTO field);

    TemplateDTO removeField(Long templateId, Long fieldId);

    TemplateDTO addCustomField(Long templateId, CustomFieldDTO customField);

    TemplateDTO removeCustomField(Long templateId, Long customFieldId);

    boolean validateTemplateData(String templateCode, Map<String, Object> data);

    Map<String, String> validateFieldValues(String templateCode, Map<String, Object> data);

    List<TemplateFieldDTO> getRequiredFields(String templateCode);

    List<TemplateFieldDTO> getFieldsByPermission(String templateCode, String permission);

    TemplateDTO copyTemplate(Long sourceId, String newTemplateCode);
}
