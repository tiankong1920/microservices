package com.inventory.templateservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.common.template.constant.TemplateConstants;
import com.inventory.common.template.dto.TemplateDTO;
import com.inventory.common.template.dto.TemplateVersionDTO;
import com.inventory.common.template.exception.TemplateException;
import com.inventory.common.template.service.ITemplateVersionService;
import com.inventory.templateservice.entity.Template;
import com.inventory.templateservice.entity.TemplateVersion;
import com.inventory.templateservice.repository.ITemplateRepository;
import com.inventory.templateservice.repository.ITemplateVersionRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TemplateVersionService implements ITemplateVersionService {

    private final ITemplateVersionRepository versionRepository;
    private final ITemplateRepository templateRepository;
    private final TemplateService templateService;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    public TemplateVersionService(ITemplateVersionRepository versionRepository,
                                   ITemplateRepository templateRepository,
                                   @Lazy TemplateService templateService,
                                   ModelMapper modelMapper,
                                   ObjectMapper objectMapper) {
        this.versionRepository = versionRepository;
        this.templateRepository = templateRepository;
        this.templateService = templateService;
        this.modelMapper = modelMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public TemplateVersionDTO createVersion(Long templateId, String changeDescription, String changeType) {
        log.info("Creating version for template: {}, type: {}", templateId, changeType);

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> TemplateException.notFound(templateId));

        String versionNumber = generateNextVersion(templateId, changeType);

        TemplateVersion version = new TemplateVersion();
        version.setTemplate(template);
        version.setVersionNumber(versionNumber);
        version.setChangeDescription(changeDescription);
        version.setChangeType(changeType);
        version.setSnapshot(captureSnapshot(template));

        String[] parts = versionNumber.split("\\.");
        version.setMajorVersion(Integer.parseInt(parts[0]));
        version.setMinorVersion(Integer.parseInt(parts[1]));
        version.setPatchVersion(Integer.parseInt(parts[2]));

        version = versionRepository.save(version);

        template.setVersion(versionNumber);
        templateRepository.save(template);

        log.info("Version created: {} for template: {}", versionNumber, templateId);
        return modelMapper.map(version, TemplateVersionDTO.class);
    }

    @Override
    public Optional<TemplateVersionDTO> getVersionById(Long versionId) {
        return versionRepository.findById(versionId)
                .map(v -> modelMapper.map(v, TemplateVersionDTO.class));
    }

    @Override
    public Optional<TemplateVersionDTO> getVersionByNumber(Long templateId, String versionNumber) {
        return versionRepository.findByTemplate_IdAndVersionNumber(templateId, versionNumber)
                .map(v -> modelMapper.map(v, TemplateVersionDTO.class));
    }

    @Override
    public List<TemplateVersionDTO> getVersionHistory(Long templateId) {
        return versionRepository.findByTemplate_IdOrderByChangedAtDesc(templateId).stream()
                .map(v -> modelMapper.map(v, TemplateVersionDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<TemplateVersionDTO> getVersionHistory(Long templateId, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, TemplateConstants.MAX_PAGE_SIZE));
        Page<TemplateVersion> versions = versionRepository.findByTemplate_IdOrderByChangedAtDesc(templateId, pageable);
        
        return versions.getContent().stream()
                .map(v -> modelMapper.map(v, TemplateVersionDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TemplateDTO rollbackToVersion(Long templateId, Long versionId, String reason) {
        log.info("Rolling back template {} to version {}", templateId, versionId);

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> TemplateException.notFound(templateId));

        TemplateVersion targetVersion = versionRepository.findById(versionId)
                .orElseThrow(() -> TemplateException.versionNotFound(versionId));

        if (!targetVersion.getTemplate().getId().equals(templateId)) {
            throw TemplateException.versionNotFound(versionId);
        }

        LocalDateTime cutoff = LocalDateTime.now().minusDays(TemplateConstants.MAX_VERSION_ROLLBACK_DAYS);
        if (targetVersion.getChangedAt().isBefore(cutoff)) {
            throw TemplateException.cannotRollback(templateId, versionId, 
                "Version is older than " + TemplateConstants.MAX_VERSION_ROLLBACK_DAYS + " days");
        }

        TemplateDTO snapshot = restoreFromSnapshot(targetVersion.getSnapshot());
        
        template.setName(snapshot.getTemplateName());
        template.setDescription(snapshot.getDescription());
        template.setCategory(snapshot.getCategory());
        template.setDefaultValues(toJson(snapshot.getDefaultValues()));
        template.setApplicableScenarios(toJson(snapshot.getApplicableScenarios()));
        template.setMetadata(toJson(snapshot.getMetadata()));

        template = templateRepository.save(template);

        TemplateVersion rollbackVersion = new TemplateVersion();
        rollbackVersion.setTemplate(template);
        rollbackVersion.setVersionNumber(generateNextVersion(templateId, TemplateConstants.CHANGE_TYPE_PATCH));
        rollbackVersion.setChangeDescription("Rollback to version " + targetVersion.getVersionNumber() + ": " + reason);
        rollbackVersion.setChangeType(TemplateConstants.CHANGE_TYPE_PATCH);
        rollbackVersion.setSnapshot(targetVersion.getSnapshot());
        rollbackVersion.setIsRollback(true);
        rollbackVersion.setRollbackFromVersionId(versionId);

        String[] parts = rollbackVersion.getVersionNumber().split("\\.");
        rollbackVersion.setMajorVersion(Integer.parseInt(parts[0]));
        rollbackVersion.setMinorVersion(Integer.parseInt(parts[1]));
        rollbackVersion.setPatchVersion(Integer.parseInt(parts[2]));

        versionRepository.save(rollbackVersion);

        log.info("Rollback completed for template {} to version {}", templateId, versionId);
        return templateService.getTemplateById(templateId).orElseThrow(() -> TemplateException.notFound(templateId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> compareVersions(Long templateId, Long versionId1, Long versionId2) {
        TemplateVersion v1 = versionRepository.findById(versionId1)
                .orElseThrow(() -> TemplateException.versionNotFound(versionId1));
        TemplateVersion v2 = versionRepository.findById(versionId2)
                .orElseThrow(() -> TemplateException.versionNotFound(versionId2));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("version1", modelMapper.map(v1, TemplateVersionDTO.class));
        result.put("version2", modelMapper.map(v2, TemplateVersionDTO.class));

        List<String> differences = new ArrayList<>();
        Map<String, Object> diffDetails = new LinkedHashMap<>();

        try {
            Map<String, Object> snapshot1 = objectMapper.readValue(v1.getSnapshot(), Map.class);
            Map<String, Object> snapshot2 = objectMapper.readValue(v2.getSnapshot(), Map.class);

            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(snapshot1.keySet());
            allKeys.addAll(snapshot2.keySet());

            for (String key : allKeys) {
                Object val1 = snapshot1.get(key);
                Object val2 = snapshot2.get(key);

                if (!Objects.equals(val1, val2)) {
                    differences.add(key);
                    Map<String, Object> change = new LinkedHashMap<>();
                    change.put("oldValue", val1);
                    change.put("newValue", val2);
                    diffDetails.put(key, change);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Error comparing versions", e);
        }

        result.put("differences", differences);
        result.put("diffDetails", diffDetails);
        result.put("totalChanges", differences.size());

        return result;
    }

    @Override
    public String generateNextVersion(Long templateId, String changeType) {
        TemplateVersion latestVersion = versionRepository
                .findFirstByTemplate_IdOrderByChangedAtDesc(templateId)
                .orElse(null);

        if (latestVersion == null) {
            return "1.0.0";
        }

        int major = latestVersion.getMajorVersion();
        int minor = latestVersion.getMinorVersion();
        int patch = latestVersion.getPatchVersion();

        switch (changeType) {
            case TemplateConstants.CHANGE_TYPE_MAJOR:
                return (major + 1) + ".0.0";
            case TemplateConstants.CHANGE_TYPE_MINOR:
                return major + "." + (minor + 1) + ".0";
            case TemplateConstants.CHANGE_TYPE_PATCH:
            default:
                return major + "." + minor + "." + (patch + 1);
        }
    }

    @Override
    public int getMajorVersion(Long templateId) {
        return versionRepository.findFirstByTemplate_IdOrderByChangedAtDesc(templateId)
                .map(TemplateVersion::getMajorVersion)
                .orElse(1);
    }

    @Override
    public int getMinorVersion(Long templateId) {
        return versionRepository.findFirstByTemplate_IdOrderByChangedAtDesc(templateId)
                .map(TemplateVersion::getMinorVersion)
                .orElse(0);
    }

    @Override
    public int getPatchVersion(Long templateId) {
        return versionRepository.findFirstByTemplate_IdOrderByChangedAtDesc(templateId)
                .map(TemplateVersion::getPatchVersion)
                .orElse(0);
    }

    @Override
    public boolean isCompatible(Long templateId, Long versionId1, Long versionId2) {
        TemplateVersion v1 = versionRepository.findById(versionId1)
                .orElseThrow(() -> TemplateException.versionNotFound(versionId1));
        TemplateVersion v2 = versionRepository.findById(versionId2)
                .orElseThrow(() -> TemplateException.versionNotFound(versionId2));

        return v1.getMajorVersion().equals(v2.getMajorVersion());
    }

    @Override
    public List<TemplateVersionDTO> getVersionsByDateRange(Long templateId, String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00", formatter);
        LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59", formatter);

        return versionRepository.findByTemplateIdAndDateRange(templateId, start, end).stream()
                .map(v -> modelMapper.map(v, TemplateVersionDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getVersionStatistics(Long templateId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        
        stats.put("totalVersions", versionRepository.countByTemplateId(templateId));
        stats.put("currentMajorVersion", getMajorVersion(templateId));
        stats.put("currentMinorVersion", getMinorVersion(templateId));
        stats.put("currentPatchVersion", getPatchVersion(templateId));

        List<Object[]> changeTypeCounts = versionRepository.countByChangeType(templateId);
        Map<String, Long> changeTypes = new LinkedHashMap<>();
        for (Object[] row : changeTypeCounts) {
            changeTypes.put((String) row[0], (Long) row[1]);
        }
        stats.put("changeTypeDistribution", changeTypes);

        return stats;
    }

    private String captureSnapshot(Template template) {
        try {
            TemplateDTO dto = templateService.getTemplateById(template.getId())
                    .orElseThrow(() -> TemplateException.notFound(template.getId()));
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.error("Error capturing snapshot", e);
            return "{}";
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

    private TemplateDTO restoreFromSnapshot(String snapshot) {
        try {
            return objectMapper.readValue(snapshot, TemplateDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Error restoring from snapshot", e);
            throw TemplateException.restoreFailed(e.getMessage());
        }
    }
}
