package com.inventory.templateservice.controller;

import com.inventory.common.template.dto.TemplateDTO;
import com.inventory.templateservice.service.TemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class TemplateBatchControllerTest {

    @Mock
    private TemplateService templateService;

    @InjectMocks
    private TemplateBatchController templateBatchController;

    private TemplateDTO testTemplate;

    @BeforeEach
    void setUp() {
        testTemplate = new TemplateDTO();
        testTemplate.setId(1L);
        testTemplate.setTemplateCode("TPL-001");
        testTemplate.setTemplateName("Test Template");
    }

    @Test
    void testBatchCreateAllSuccess() {
        when(templateService.createTemplate(any(TemplateDTO.class))).thenReturn(testTemplate);

        ResponseEntity<Map<String, Object>> response =
                templateBatchController.batchCreate(List.of(testTemplate, testTemplate), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().get("total"));
        assertEquals(2, response.getBody().get("success"));
        assertEquals(0, response.getBody().get("failed"));
    }

    @Test
    void testBatchCreateWithErrorStopsImmediately() {
        when(templateService.createTemplate(any(TemplateDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<Map<String, Object>> response =
                templateBatchController.batchCreate(List.of(testTemplate), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().get("failed"));
    }

    @Test
    void testBatchCreateWithErrorContinues() {
        when(templateService.createTemplate(any(TemplateDTO.class)))
                .thenReturn(testTemplate)
                .thenThrow(new RuntimeException("Error on second"));

        ResponseEntity<Map<String, Object>> response =
                templateBatchController.batchCreate(List.of(testTemplate, testTemplate), true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().get("total"));
        assertEquals(1, response.getBody().get("success"));
        assertEquals(1, response.getBody().get("failed"));
    }

    @Test
    void testBatchUpdate() {
        when(templateService.updateTemplate(any(), any())).thenReturn(testTemplate);

        ResponseEntity<Map<String, Object>> response =
                templateBatchController.batchUpdate(List.of(testTemplate), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().get("success"));
    }

    @Test
    void testBatchUpdateWithNullId() {
        TemplateDTO noId = new TemplateDTO();
        noId.setTemplateCode("NO-ID");

        ResponseEntity<Map<String, Object>> response =
                templateBatchController.batchUpdate(List.of(noId), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().get("success"));
    }

    @Test
    void testBatchDeleteAllSuccess() {
        doNothing().when(templateService).deleteTemplate(any());

        ResponseEntity<Map<String, Object>> response =
                templateBatchController.batchDelete(List.of(1L, 2L, 3L), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().get("total"));
        assertEquals(3, response.getBody().get("success"));
    }

    @Test
    void testBatchDeleteWithError() {
        doNothing().when(templateService).deleteTemplate(1L);
        doNothing().when(templateService).deleteTemplate(3L);
        doThrow(new RuntimeException("Not found")).when(templateService).deleteTemplate(2L);

        ResponseEntity<Map<String, Object>> response =
                templateBatchController.batchDelete(List.of(1L, 2L, 3L), true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().get("success"));
        assertEquals(1, response.getBody().get("failed"));
    }

    @Test
    void testBatchPublish() {
        when(templateService.publishTemplate(any())).thenReturn(testTemplate);

        ResponseEntity<Map<String, Object>> response =
                templateBatchController.batchPublish(List.of(1L, 2L), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().get("success"));
    }

    @Test
    void testBatchDeprecate() {
        when(templateService.deprecateTemplate(any())).thenReturn(testTemplate);

        ResponseEntity<Map<String, Object>> response =
                templateBatchController.batchDeprecate(List.of(1L), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().get("success"));
        verify(templateService).deprecateTemplate(1L);
    }

    @Test
    void testBatchArchive() {
        when(templateService.archiveTemplate(any())).thenReturn(testTemplate);

        ResponseEntity<Map<String, Object>> response =
                templateBatchController.batchArchive(List.of(1L, 2L), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().get("success"));
    }
}
