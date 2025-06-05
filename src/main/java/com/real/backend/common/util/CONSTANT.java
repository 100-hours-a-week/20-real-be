package com.real.backend.common.util;

public class CONSTANT {

    private CONSTANT() {}

    // JWT Expire time
    public static final long ACCESS_TOKEN_EXPIRED = 60 * 60L; // 1시간
    public static final long REFRESH_TOKEN_EXPIRED = 14 * 24 * 60 * 60L; // 14일

}
