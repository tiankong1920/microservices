package com.inventory.authservice.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    
    String[] value() default {};
    
    RequireRoleMode mode() default RequireRoleMode.ANY;
    
    String message() default "Access denied. Required role not found.";
    
    enum RequireRoleMode {
        ALL,
        ANY
    }
}
