package com.real.backend.common.exception;

public class ServerException extends RuntimeException {
    public ServerException(String message) {
        super(message);
    }
}
