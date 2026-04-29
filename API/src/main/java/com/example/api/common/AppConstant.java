package com.example.api.common;

public final class AppConstant {

    private AppConstant() {}  // util class — chặn instantiate

    // Role names — phải khớp với seed data trong V2 migration
    public static final String CUSTOMER_ROLE = "CUSTOMER";
    public static final String DRIVER_ROLE   = "DRIVER";
    public static final String MERCHANT_ROLE = "MERCHANT";
    public static final String ADMIN_ROLE    = "ADMIN";

    public static final String AUTHORITIES = "authorities";
    public static final String TOKEN_TYPE  = "type";
    public static final String BEARER      = "Bearer ";
}
