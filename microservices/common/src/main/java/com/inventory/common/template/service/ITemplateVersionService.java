package com.inventory.common.template.service;

import com.inventory.common.template.dto.TemplateDTO;
import com.inventory.common.template.dto.TemplateVersionDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ITemplateVersionService {

    TemplateVersionDTO createVersion(Long templateId, String changeDescription, String changeType);

    Optional<TemplateVersionDTO> getVersionById(Long versionId);

    Optional<TemplateVersionDTO> getVersionByNumber(Long templateId, String versionNumber);

    List<TemplateVersionDTO> getVersionHistory(Long templateId);

    List<TemplateVersionDTO> getVersionHistory(Long templateId, int page, int size);

    TemplateDTO rollbackToVersion(Long templateId, Long versionId, String reason);

    Map<String, Object> compareVersions(Long templateId, Long versionId1, Long versionId2);

    String generateNextVersion(Long templateId, String changeType);

    int getMajorVersion(Long templateId);

    int getMinorVersion(Long templateId);

    int getPatchVersion(Long templateId);

    boolean isCompatible(Long templateId, Long versionId1, Long versionId2);

    List<TemplateVersionDTO> getVersionsByDateRange(Long templateId, String startDate, String endDate);

    Map<String, Object> getVersionStatistics(Long templateId);
}
