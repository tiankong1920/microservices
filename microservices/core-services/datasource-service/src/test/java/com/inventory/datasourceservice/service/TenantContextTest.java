package com.inventory.datasourceservice.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantContext Unit Tests")
class TenantContextTest {

    private TenantContext tenantContext;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private ServletRequestAttributes mockAttributes;

    @BeforeEach
    void setUp() {
        tenantContext = new TenantContext();
    }

    @Nested
    @DisplayName("getCurrentTenant() method tests")
    class GetCurrentTenantTests {

        @Test
        @DisplayName("should return tenant id from header")
        void shouldReturnTenantIdFromHeader() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(mockAttributes);
                when(mockAttributes.getRequest()).thenReturn(mockRequest);
                when(mockRequest.getHeader("X-Tenant-Id")).thenReturn("tenant-123");

                String tenantId = tenantContext.getCurrentTenant();

                assertEquals("tenant-123", tenantId);
            }
        }

        @Test
        @DisplayName("should return default when no request attributes")
        void shouldReturnDefaultWhenNoRequestAttributes() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                String tenantId = tenantContext.getCurrentTenant();

                assertEquals("default", tenantId);
            }
        }

        @Test
        @DisplayName("should return default when header is empty")
        void shouldReturnDefaultWhenHeaderIsEmpty() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(mockAttributes);
                when(mockAttributes.getRequest()).thenReturn(mockRequest);
                when(mockRequest.getHeader("X-Tenant-Id")).thenReturn("");

                String tenantId = tenantContext.getCurrentTenant();

                assertEquals("default", tenantId);
            }
        }

        @Test
        @DisplayName("should return default when header is null")
        void shouldReturnDefaultWhenHeaderIsNull() {
            try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
                mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(mockAttributes);
                when(mockAttributes.getRequest()).thenReturn(mockRequest);
                when(mockRequest.getHeader("X-Tenant-Id")).thenReturn(null);

                String tenantId = tenantContext.getCurrentTenant();

                assertEquals("default", tenantId);
            }
        }
    }

    @Nested
    @DisplayName("setCurrentTenant() method tests")
    class SetCurrentTenantTests {

        @Test
        @DisplayName("should set tenant id in thread local")
        void shouldSetTenantIdInThreadLocal() {
            tenantContext.setCurrentTenant("tenant-456");

            tenantContext.clear();
        }

        @Test
        @DisplayName("should allow setting different tenant ids")
        void shouldAllowSettingDifferentTenantIds() {
            tenantContext.setCurrentTenant("tenant-001");
            tenantContext.clear();

            tenantContext.setCurrentTenant("tenant-002");
            tenantContext.clear();
        }
    }

    @Nested
    @DisplayName("clear() method tests")
    class ClearTests {

        @Test
        @DisplayName("should clear tenant id from thread local")
        void shouldClearTenantIdFromThreadLocal() {
            tenantContext.setCurrentTenant("tenant-789");
            tenantContext.clear();

            tenantContext.setCurrentTenant("tenant-new");
            tenantContext.clear();
        }

        @Test
        @DisplayName("should be safe to call clear multiple times")
        void shouldBeSafeToCallClearMultipleTimes() {
            tenantContext.clear();
            tenantContext.clear();
            tenantContext.clear();
        }
    }
}
