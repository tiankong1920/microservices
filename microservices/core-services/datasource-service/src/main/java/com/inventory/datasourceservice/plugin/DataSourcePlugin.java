package com.inventory.datasourceservice.plugin;

import com.inventory.datasourceservice.dto.ConnectionTestResultDTO;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface DataSourcePlugin {

    String getPluginName();

    String getPluginVersion();

    DatasourceConfig.DatasourceType getSupportedType();

    List<String> getSupportedVersions();

    ConnectionTestResultDTO testConnection(DatasourceConfig config);

    String detectVersion(DatasourceConfig config);

    MetadataInfo discoverMetadata(DatasourceConfig config);

    Map<String, Object> getDefaultConfig();

    List<ConfigField> getConfigFields();

    List<String> diagnoseError(String errorCode, String errorMessage);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class MetadataInfo {
        private String catalog;
        private List<TableInfo> tables;
        private List<FunctionInfo> functions;
        private String version;
        private Map<String, Object> properties;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class TableInfo {
        private String name;
        private String schema;
        private String type;
        private String comment;
        private List<ColumnInfo> columns;
        private List<String> primaryKeys;
        private List<IndexInfo> indexes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ColumnInfo {
        private String name;
        private String type;
        private Boolean nullable;
        private String defaultValue;
        private String comment;
        private Integer position;
        private Integer length;
        private Integer precision;
        private Integer scale;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class IndexInfo {
        private String name;
        private List<String> columns;
        private Boolean unique;
        
        public void addColumn(String column) {
            if (columns == null) {
                columns = new ArrayList<>();
            }
            columns.add(column);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class FunctionInfo {
        private String name;
        private String returnType;
        private List<String> parameters;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ConfigField {
        private String name;
        private String label;
        private String type;
        private String defaultValue;
        private Boolean required;
        private String description;
        private String placeholder;
        private String validationRegex;
        private List<String> options;
        private Integer minLength;
        private Integer maxLength;
        private Integer minValue;
        private Integer maxValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ConfigSchema {
        private DatasourceConfig.DatasourceType type;
        private List<ConfigField> fields;
        private Map<String, Object> defaults;
    }
}
