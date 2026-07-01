package com.hostel.util;

public final class AppConstants {
    private AppConstants() { }

    public static final String APP_NAME = "Hostel Management System";
    public static final String APP_VERSION = "1.0.0";

    // Socket chat server defaults
    public static final int CHAT_SERVER_PORT = 5050;
    public static final String CHAT_SERVER_HOST = "localhost";

    // Default admin bootstrap credentials (used only if DB still has the placeholder hash)
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";

    public static final String REPORTS_DIR = "reports";

    public static final String[] USER_ROLES = { "Admin", "Girl Student", "Parent / Guardian" };
}
