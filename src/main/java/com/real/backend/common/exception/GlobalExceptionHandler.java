package com.real.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.HandlerMapping;

import com.real.backend.common.response.StatusResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<StatusResponse> handleBadRequest(BadRequestException e) {
        StatusResponse response = StatusResponse.of(400, e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StatusResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().isEmpty() ?
            "Invalid arguments" :
            e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        StatusResponse response = StatusResponse.of(400, errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<StatusResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        String parameterName = e.getName();
        String parameterType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";
        String value = e.getValue() != null ? e.getValue().toString() : "null";
        String message = String.format("Parameter '%s' with value '%s' could not be converted to type '%s'", parameterName, value, parameterType);

        StatusResponse response = StatusResponse.of(400, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<StatusResponse> handleMissingParam(MissingServletRequestParameterException e) {
        StatusResponse response = StatusResponse.of(400, "Required parameter not found.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StatusResponse> handleMessageNotReadable(HttpMessageNotReadableException e) {
        StatusResponse response = StatusResponse.of(400, "Message format is incorrect.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<StatusResponse> handleUnauthorized(UnauthorizedException e) {
        StatusResponse response = StatusResponse.of(401, e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<StatusResponse> handleForbidden(ForbiddenException e) {
        StatusResponse response = StatusResponse.of(403, e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<StatusResponse> handleNotFound(NotFoundException e) {
        StatusResponse response = StatusResponse.of(404, e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<StatusResponse> handleServer(ServerException e) {
        StatusResponse response = StatusResponse.of(500, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<StatusResponse> handleRuntime(RuntimeException e) {
        StatusResponse response = StatusResponse.of(500, e);
        log.error("RuntimeException 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StatusResponse> handleAccessDenied(
        AccessDeniedException e,
        HttpServletRequest request
    ) {
        String message = "권한이 없습니다.";

        Object attr = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (attr instanceof HandlerMethod handlerMethod) {
            // 2) 메서드 레벨 @PreAuthorize 읽기
            PreAuthorize PreAuthorize = handlerMethod.getMethodAnnotation(PreAuthorize.class);

            if (PreAuthorize == null) {
                PreAuthorize = handlerMethod.getBeanType().getAnnotation(PreAuthorize.class);
            }

            if (PreAuthorize != null) {
                String expr = PreAuthorize.value();                  // 절대로 pa가 null일 때 여기 안 옴
                // String norm = expr.replaceAll("\\s+", ""); // 공백 제거

                if (expr.contains("OUTSIDER") && expr.contains("TRAINEE")) {
                    message = "운영진만 접근할 수 있습니다.";
                } else if (expr.contains("OUTSIDER")) {
                    message = "외부인은 접근할 수 없습니다.";
                }
            }
        }

        StatusResponse response = StatusResponse.of(403, message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<StatusResponse> handleMethodValidationException(HandlerMethodValidationException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(StatusResponse.of(400, "파라미터 혹은 리소스 속성 값이 제한 설정을 초과했습니다."));
    }
}
