package com.real.backend.exception;

public class ServerException extends RuntimeException {
    public ServerException(String message) {
        super(message);
    }
}
