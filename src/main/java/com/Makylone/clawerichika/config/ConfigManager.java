package com.Makylone.clawerichika.config;

public class ConfigManager {

    public static String GetToken() {
        return System.getenv("TOKEN_DISCORD");
    }

    public static String GetAdminRoleId() {
        return System.getenv("ADMIN_ROLE_ID");
    }
}
