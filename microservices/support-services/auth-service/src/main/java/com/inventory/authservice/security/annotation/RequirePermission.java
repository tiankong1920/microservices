package com.inventory.authservice.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    
    String[] value() default {};
    
    RequireMode mode() default RequireMode.ALL;
    
    String message() default "Access denied. Required permission not found.";
    
    enum RequireMode {
        ALL,
        ANY
    }
}
