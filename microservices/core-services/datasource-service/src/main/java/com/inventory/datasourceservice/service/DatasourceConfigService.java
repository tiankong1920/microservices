package com.inventory.datasourceservice.service;

import com.inventory.datasourceservice.dto.DatasourceConfigDTO;
import com.inventory.datasourceservice.dto.PageResponse;
import com.inventory.datasourceservice.entity.AuditLog;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import com.inventory.datasourceservice.repository.IAuditLogRepository;
import com.inventory.datasourceservice.repository.IConnectionStatusRepository;
import com.inventory.datasourceservice.repository.IConnectionTestLogRepository;
import com.inventory.datasourceservice.repository.IDatasourceConfigRepository;
import com.inventory.datasourceservice.plugin.DataSourcePlugin;
import com.inventory.datasourceservice.plugin.PluginRegistry;
import com.inventory.datasourceservice.security.EncryptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatasourceConfigService {

    private final IDatasourceConfigRepository datasourceConfigRepository;
    private final IConnectionStatusRepository connectionStatusRepository;
    private final IConnectionTestLogRepository connectionTestLogRepository;
    private final IAuditLogRepository auditLogRepository;
    private final PluginRegistry pluginRegistry;
    private final ObjectMapper objectMapper;
    private final TenantContext tenantContext;
    private final UserContext userContext;
    private final EncryptionService encryptionService;

    @Transactional
    @CacheEvict(value = "datasource-configs", allEntries = true)
    public DatasourceConfigDTO createDatasource(DatasourceConfigDTO dto) {
        String tenantId = tenantContext.getCurrentTenant();
        
        if (datasourceConfigRepository.existsByNameAndTenantId(dto.getName(), tenantId)) {
            throw new IllegalArgumentException("数据源名称已存在: " + dto.getName());
        }

        DatasourceConfig config = DatasourceConfig.builder()
                .tenantId(tenantId)
                .name(dto.getName())
                .type(dto.getType())
                .version(dto.getVersion())
                .host(dto.getHost())
                .port(dto.getPort())
                .databaseName(dto.getDatabaseName())
                .username(dto.getUsername())
                .password(encryptionService.encrypt(dto.getPassword()))
                .extraConfig(dto.getExtraConfig())
                .templateId(dto.getTemplateId())
                .status(DatasourceConfig.DatasourceStatus.ACTIVE)
                .createdBy(userContext.getCurrentUserId())
                .build();

        config = datasourceConfigRepository.save(config);

        auditLog(AuditLog.Operation.CREATE, "DatasourceConfig", config.getId().toString(), null, config);

        log.info("Created datasource: {} for tenant: {}", config.getName(), tenantId);
        
        return toDTO(config);
    }

    @Transactional
    @CacheEvict(value = "datasource-configs", key = "#id")
    public DatasourceConfigDTO updateDatasource(Long id, DatasourceConfigDTO dto) {
        String tenantId = tenantContext.getCurrentTenant();
        
        DatasourceConfig existing = datasourceConfigRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("数据源不存在: " + id));

        DatasourceConfig oldValue = copyEntity(existing);

        existing.setName(dto.getName());
        existing.setVersion(dto.getVersion());
        existing.setHost(dto.getHost());
        existing.setPort(dto.getPort());
        existing.setDatabaseName(dto.getDatabaseName());
        existing.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            existing.setPassword(encryptionService.encrypt(dto.getPassword()));
        }
        existing.setExtraConfig(dto.getExtraConfig());
        existing.setUpdatedBy(userContext.getCurrentUserId());

        existing = datasourceConfigRepository.save(existing);

        auditLog(AuditLog.Operation.UPDATE, "DatasourceConfig", existing.getId().toString(), oldValue, existing);

        log.info("Updated datasource: {} for tenant: {}", existing.getName(), tenantId);
        
        return toDTO(existing);
    }

    @Transactional
    @CacheEvict(value = "datasource-configs", key = "#id")
    public void deleteDatasource(Long id) {
        String tenantId = tenantContext.getCurrentTenant();
        
        DatasourceConfig config = datasourceConfigRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("数据源不存在: " + id));

        config.setStatus(DatasourceConfig.DatasourceStatus.DELETED);
        config.setUpdatedBy(userContext.getCurrentUserId());
        datasourceConfigRepository.save(config);

        connectionStatusRepository.deleteByDatasourceId(id);

        auditLog(AuditLog.Operation.DELETE, "DatasourceConfig", id.toString(), config, null);

        log.info("Deleted datasource: {} for tenant: {}", config.getName(), tenantId);
    }

    @Cacheable(value = "datasource-configs", key = "#id")
    public DatasourceConfigDTO getDatasource(Long id) {
        String tenantId = tenantContext.getCurrentTenant();
        
        DatasourceConfig config = datasourceConfigRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("数据源不存在: " + id));
        
        return toDTO(config);
    }

    public PageResponse<DatasourceConfigDTO> listDatasources(int page, int size, String sortBy, String sortDir) {
        String tenantId = tenantContext.getCurrentTenant();
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<DatasourceConfig> configs = datasourceConfigRepository.findByTenantId(tenantId, pageable);
        
        List<DatasourceConfigDTO> dtos = configs.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return PageResponse.of(dtos, configs.getTotalElements(), page, size);
    }

    public PageResponse<DatasourceConfigDTO> searchDatasources(String name, DatasourceConfig.DatasourceType type,
                                                                 DatasourceConfig.DatasourceStatus status,
                                                                 int page, int size) {
        String tenantId = tenantContext.getCurrentTenant();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<DatasourceConfig> configs = datasourceConfigRepository.search(tenantId, name, type, status, pageable);
        
        List<DatasourceConfigDTO> dtos = configs.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return PageResponse.of(dtos, configs.getTotalElements(), page, size);
    }

    public List<DatasourceConfigDTO> listByType(DatasourceConfig.DatasourceType type) {
        String tenantId = tenantContext.getCurrentTenant();
        
        return datasourceConfigRepository.findByTenantIdAndType(tenantId, type).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DataSourcePlugin.ConfigSchema getConfigSchema(DatasourceConfig.DatasourceType type) {
        DataSourcePlugin plugin = pluginRegistry.getPlugin(type)
                .orElseThrow(() -> new IllegalArgumentException("不支持的数据源类型: " + type));
        
        return DataSourcePlugin.ConfigSchema.builder()
                .type(type)
                .fields(plugin.getConfigFields())
                .defaults(plugin.getDefaultConfig())
                .build();
    }

    private DatasourceConfigDTO toDTO(DatasourceConfig config) {
        DatasourceConfigDTO dto = DatasourceConfigDTO.builder()
                .id(config.getId())
                .name(config.getName())
                .type(config.getType())
                .version(config.getVersion())
                .host(config.getHost())
                .port(config.getPort())
                .databaseName(config.getDatabaseName())
                .username(config.getUsername())
                .extraConfig(config.getExtraConfig())
                .status(config.getStatus())
                .templateId(config.getTemplateId())
                .createdBy(config.getCreatedBy())
                .createdAt(config.getCreatedAt())
                .updatedBy(config.getUpdatedBy())
                .updatedAt(config.getUpdatedAt())
                .build();

        connectionStatusRepository.findFirstByDatasourceIdOrderByCheckedAtDesc(config.getId())
                .ifPresent(status -> dto.setConnectionStatus(
                        DatasourceConfigDTO.ConnectionStatusDTO.builder()
                                .status(status.getStatus().name())
                                .responseTime(status.getResponseTime())
                                .errorMessage(status.getErrorMessage())
                                .checkedAt(status.getCheckedAt())
                                .build()
                ));

        return dto;
    }

    private DatasourceConfig copyEntity(DatasourceConfig source) {
        return DatasourceConfig.builder()
                .id(source.getId())
                .tenantId(source.getTenantId())
                .name(source.getName())
                .type(source.getType())
                .version(source.getVersion())
                .host(source.getHost())
                .port(source.getPort())
                .databaseName(source.getDatabaseName())
                .username(source.getUsername())
                .password(source.getPassword())
                .extraConfig(source.getExtraConfig())
                .status(source.getStatus())
                .templateId(source.getTemplateId())
                .createdBy(source.getCreatedBy())
                .createdAt(source.getCreatedAt())
                .updatedBy(source.getUpdatedBy())
                .updatedAt(source.getUpdatedAt())
                .build();
    }

    private void auditLog(AuditLog.Operation operation, String resourceType, String resourceId,
                          Object oldValue, Object newValue) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .tenantId(tenantContext.getCurrentTenant())
                    .userId(userContext.getCurrentUserId())
                    .username(userContext.getCurrentUsername())
                    .operation(operation)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
                    .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                    .ipAddress(userContext.getClientIp())
                    .userAgent(userContext.getUserAgent())
                    .build();
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }
}
