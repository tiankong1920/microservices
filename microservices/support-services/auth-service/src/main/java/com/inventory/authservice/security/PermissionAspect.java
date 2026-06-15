package com.inventory.authservice.security;

import com.inventory.authservice.security.annotation.RequirePermission;
import com.inventory.authservice.security.annotation.RequireRole;
import com.inventory.authservice.service.DynamicPermissionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

@Aspect
@Component
public class PermissionAspect {

    private final DynamicPermissionService permissionService;

    public PermissionAspect(DynamicPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Around("@annotation(com.inventory.authservice.security.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        String username = authentication.getName();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        
        String[] requiredPermissions = annotation.value();
        RequirePermission.RequireMode mode = annotation.mode();
        
        Set<String> userPermissions = permissionService.getUserPermissions(username);
        
        boolean hasPermission;
        if (mode == RequirePermission.RequireMode.ALL) {
            hasPermission = Arrays.stream(requiredPermissions)
                .allMatch(userPermissions::contains);
        } else {
            hasPermission = Arrays.stream(requiredPermissions)
                .anyMatch(userPermissions::contains);
        }
        
        if (!hasPermission) {
            throw new AccessDeniedException(annotation.message());
        }
        
        return joinPoint.proceed();
    }

    @Around("@annotation(com.inventory.authservice.security.annotation.RequireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        String username = authentication.getName();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole annotation = method.getAnnotation(RequireRole.class);
        
        String[] requiredRoles = annotation.value();
        RequireRole.RequireRoleMode mode = annotation.mode();
        
        Set<String> userRoles = permissionService.getUserRoles(username);
        
        boolean hasRole;
        if (mode == RequireRole.RequireRoleMode.ALL) {
            hasRole = Arrays.stream(requiredRoles)
                .allMatch(userRoles::contains);
        } else {
            hasRole = Arrays.stream(requiredRoles)
                .anyMatch(userRoles::contains);
        }
        
        if (!hasRole) {
            throw new AccessDeniedException(annotation.message());
        }
        
        return joinPoint.proceed();
    }
}
