package com.real.backend.common.util;

public class CONSTANT {

    private CONSTANT() {}

    // JWT Expire time
    public static final long ACCESS_TOKEN_EXPIRED = 60 * 60L; // 1시간
    public static final long REFRESH_TOKEN_EXPIRED = 14 * 24 * 60 * 60L; // 14일

    // Cookie name
    public static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN_V2";
    public static final String REFRESH_TOKEN_COOKIE = "ACCESS_TOKEN_V2";
}
