package com.Makylone.clawerichika.config;

import io.github.cdimascio.dotenv.Dotenv;

public class ConfigManager {

    private static final Dotenv dotenv = Dotenv.load();

    public static String GetToken() {
        return dotenv.get("TOKEN_DISCORD");
    }

    public static String GetAdminRoleId() {
        return dotenv.get("ADMIN_ROLE_ID");
    }
}
