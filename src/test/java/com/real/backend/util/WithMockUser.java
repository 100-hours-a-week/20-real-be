package com.real.backend.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = MockUserFactory.class)
public @interface WithMockUser {

    long id() default 1L;
    String username() default "test@test.com";
    String role() default "STAFF";
}
