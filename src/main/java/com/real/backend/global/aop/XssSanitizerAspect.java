package com.real.backend.global.aop;

import java.lang.reflect.Field;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class XssSanitizerAspect {

    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping) || "
        + "@annotation(org.springframework.web.bind.annotation.PutMapping) || "
        + "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public Object sanitizeDtoFields(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        for (Object arg : args) {
            if (arg == null) continue;

            for (Field field : arg.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Sanitizer.class) && field.getType().equals(String.class)) {
                    field.setAccessible(true);
                    String raw = (String) field.get(arg);
                    if (raw != null) {
                        String clean = XssSanitizer.sanitize(raw);
                        field.set(arg, clean);
                    }
                }
            }
        }

        return joinPoint.proceed(args);
    }
}
