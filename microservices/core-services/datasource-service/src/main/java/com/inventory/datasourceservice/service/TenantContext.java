package com.inventory.datasourceservice.service;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class TenantContext {

    private static final String TENANT_HEADER = "X-Tenant-Id";
    private static final String DEFAULT_TENANT = "default";

    public String getCurrentTenant() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String tenantId = request.getHeader(TENANT_HEADER);
            if (tenantId != null && !tenantId.isEmpty()) {
                return tenantId;
            }
        }
        return DEFAULT_TENANT;
    }

    public void setCurrentTenant(String tenantId) {
        TenantHolder.setTenantId(tenantId);
    }

    public void clear() {
        TenantHolder.clear();
    }

    private static class TenantHolder {
        private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

        public static void setTenantId(String tenantId) {
            TENANT_ID.set(tenantId);
        }

        public static String getTenantId() {
            return TENANT_ID.get();
        }

        public static void clear() {
            TENANT_ID.remove();
        }
    }
}
