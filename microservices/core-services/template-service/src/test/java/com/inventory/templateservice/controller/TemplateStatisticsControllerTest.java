package com.inventory.templateservice.controller;

import com.inventory.common.template.BusinessDomain;
import com.inventory.common.template.TemplateStatus;
import com.inventory.templateservice.repository.ITemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("null")
class TemplateStatisticsControllerTest {

    @Mock
    private ITemplateRepository templateRepository;

    @InjectMocks
    private TemplateStatisticsController templateStatisticsController;

    @Test
    void testGetOverview() {
        lenient().when(templateRepository.count()).thenReturn(100L);
        lenient().when(templateRepository.countByStatus(TemplateStatus.PUBLISHED)).thenReturn(60L);
        lenient().when(templateRepository.countByBusinessDomain(BusinessDomain.INVENTORY_MANAGEMENT)).thenReturn(50L);

        ResponseEntity<Map<String, Object>> response = templateStatisticsController.getOverview();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().get("totalTemplates"));
        assertNotNull(response.getBody().get("byStatus"));
        assertNotNull(response.getBody().get("byDomain"));
    }

    @Test
    void testGetByStatus() {
        lenient().when(templateRepository.countByStatus(TemplateStatus.PUBLISHED)).thenReturn(60L);
        lenient().when(templateRepository.countByStatus(TemplateStatus.DRAFT)).thenReturn(30L);

        ResponseEntity<Map<String, Long>> response = templateStatisticsController.getByStatus();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetByDomain() {
        lenient().when(templateRepository.countByBusinessDomain(BusinessDomain.INVENTORY_MANAGEMENT)).thenReturn(50L);

        ResponseEntity<Map<String, Long>> response = templateStatisticsController.getByDomain();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetByTenant() {
        lenient().when(templateRepository.countByTenantId("tenant-001")).thenReturn(20L);
        lenient().when(templateRepository.countByTenantIdAndStatus("tenant-001", TemplateStatus.PUBLISHED)).thenReturn(10L);

        ResponseEntity<Map<String, Object>> response = templateStatisticsController.getByTenant("tenant-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("tenant-001", response.getBody().get("tenantId"));
        assertEquals(20L, response.getBody().get("totalTemplates"));
    }

    @Test
    void testGetTrend() {
        ResponseEntity<Map<String, Object>> response = templateStatisticsController.getTrend(30);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("30 days", response.getBody().get("period"));
    }

    @Test
    void testGetDashboard() {
        lenient().when(templateRepository.count()).thenReturn(100L);
        lenient().when(templateRepository.countByStatus(TemplateStatus.PUBLISHED)).thenReturn(60L);
        lenient().when(templateRepository.countByStatus(TemplateStatus.DRAFT)).thenReturn(30L);
        lenient().when(templateRepository.countByStatus(TemplateStatus.DEPRECATED)).thenReturn(10L);
        lenient().when(templateRepository.countByBusinessDomain(BusinessDomain.INVENTORY_MANAGEMENT)).thenReturn(50L);
        lenient().when(templateRepository.countByBusinessDomain(BusinessDomain.ORDER_PROCESSING)).thenReturn(20L);

        ResponseEntity<Map<String, Object>> response = templateStatisticsController.getDashboard();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().get("totalTemplates"));
        assertEquals(60L, response.getBody().get("activeTemplates"));
        assertEquals(30L, response.getBody().get("draftTemplates"));
        assertEquals(10L, response.getBody().get("deprecatedTemplates"));
        assertNotNull(response.getBody().get("topDomains"));
    }
}
