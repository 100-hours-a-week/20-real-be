package com.real.backend.common.util;

public class CONSTANT {

    private CONSTANT() {}

    // JWT Expire time
    public static final long ACCESS_TOKEN_EXPIRED = 5 * 60L; // 5분
    public static final long REFRESH_TOKEN_EXPIRED = 14 * 24 * 60 * 60L; // 14일

    // SSE connection time
    public static final long CONNECTION_TIMEOUT = 5 * 60 * 1000L; // 5분
    public static final long HEARTBEAT_INTERVAL = 10 * 1000L; // 10초

}
