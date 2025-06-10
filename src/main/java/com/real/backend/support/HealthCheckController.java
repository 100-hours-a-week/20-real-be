package com.real.backend.support;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthCheckController {

    @GetMapping("/healthz")
    public ResponseEntity<Boolean> healthCheck() {
        return ResponseEntity.ok(Boolean.TRUE);
    }
}
