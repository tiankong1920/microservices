package com.inventory.datasourceservice.dto;

import com.inventory.datasourceservice.entity.ConnectionTestLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionTestResultDTO {

    private Long datasourceId;

    private String datasourceName;

    private ConnectionTestLog.TestResult result;

    private Integer responseTime;

    private String errorCode;

    private String errorMessage;

    private List<String> suggestions;

    private LocalDateTime testedAt;

    public static ConnectionTestResultDTO success(Long datasourceId, String datasourceName, Integer responseTime) {
        return ConnectionTestResultDTO.builder()
                .datasourceId(datasourceId)
                .datasourceName(datasourceName)
                .result(ConnectionTestLog.TestResult.SUCCESS)
                .responseTime(responseTime)
                .testedAt(LocalDateTime.now())
                .build();
    }

    public static ConnectionTestResultDTO failure(Long datasourceId, String datasourceName, 
                                                   String errorCode, String errorMessage,
                                                   List<String> suggestions) {
        return ConnectionTestResultDTO.builder()
                .datasourceId(datasourceId)
                .datasourceName(datasourceName)
                .result(ConnectionTestLog.TestResult.FAILURE)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .suggestions(suggestions)
                .testedAt(LocalDateTime.now())
                .build();
    }

    public static ConnectionTestResultDTO failure(Long datasourceId, String datasourceName, 
                                                   String errorCode, String errorMessage,
                                                   List<String> suggestions, Integer responseTime) {
        return ConnectionTestResultDTO.builder()
                .datasourceId(datasourceId)
                .datasourceName(datasourceName)
                .result(ConnectionTestLog.TestResult.FAILURE)
                .responseTime(responseTime)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .suggestions(suggestions)
                .testedAt(LocalDateTime.now())
                .build();
    }

    public static ConnectionTestResultDTO timeout(Long datasourceId, String datasourceName) {
        return ConnectionTestResultDTO.builder()
                .datasourceId(datasourceId)
                .datasourceName(datasourceName)
                .result(ConnectionTestLog.TestResult.TIMEOUT)
                .errorMessage("连接超时，请检查网络或防火墙设置")
                .testedAt(LocalDateTime.now())
                .build();
    }
}
