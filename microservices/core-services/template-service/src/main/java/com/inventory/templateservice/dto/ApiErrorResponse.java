package com.inventory.templateservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String traceId;
    private List<FieldError> fieldErrors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }

    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    public static ApiErrorResponse badRequest(String message, String path) {
        return of(400, "Bad Request", message, path);
    }

    public static ApiErrorResponse notFound(String message, String path) {
        return of(404, "Not Found", message, path);
    }

    public static ApiErrorResponse conflict(String message, String path) {
        return of(409, "Conflict", message, path);
    }

    public static ApiErrorResponse internalError(String message, String path) {
        return of(500, "Internal Server Error", message, path);
    }
}
