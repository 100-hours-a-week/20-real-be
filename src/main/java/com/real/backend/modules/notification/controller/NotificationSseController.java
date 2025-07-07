package com.real.backend.modules.notification.controller;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.real.backend.modules.notification.service.NotificationSseService;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.SecurityContextUtil;
import com.real.backend.security.Session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NotificationSseController {

    private final NotificationSseService notificationSseService;

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @GetMapping(value="/v1/connect/notification", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @CurrentSession Session session) {
        SecurityContextUtil.propagateSecurityContextToRequest(httpServletRequest, httpServletResponse);
        return notificationSseService.connect(session.getId());
    }
}
