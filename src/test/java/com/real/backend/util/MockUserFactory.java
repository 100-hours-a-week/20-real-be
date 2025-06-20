package com.real.backend.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.real.backend.security.Session;

public class MockUserFactory implements WithSecurityContextFactory<WithMockUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockUser annotation) {

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Session session = new Session(annotation.id(), annotation.username(), annotation.role());
        Authentication authToken = new UsernamePasswordAuthenticationToken(session, null, session.getAuthorities());
        context.setAuthentication(authToken);

        return context;
    }
}
