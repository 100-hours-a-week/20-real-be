package com.real.backend.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResponseDTO {

    private final int code;
    private final String message;

    public ResponseDTO(HttpStatus status, String message) {
        this.code = status.value();
        this.message = message;
    }

}
