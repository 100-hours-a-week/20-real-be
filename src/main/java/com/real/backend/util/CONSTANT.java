package com.real.backend.util;

public class CONSTANT {

    private CONSTANT() {}

    // JWT Header
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final long ACCESS_TOKEN_EXPIRED = 60 * 60L; // 1시간
    public static final long REFRESH_TOKEN_EXPIRED = 14 * 24 * 60 * 60L; // 14일

}
