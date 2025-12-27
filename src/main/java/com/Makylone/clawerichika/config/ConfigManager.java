package com.Makylone.clawerichika.config;

import io.github.cdimascio.dotenv.Dotenv;

public class ConfigManager {

    public static final Dotenv DOTENV = Dotenv.load();

    public static String GetToken() {
        return DOTENV.get("TOKEN_DISCORD");
    }

    public static String GetAdminRoleId() {
        return DOTENV.get("ADMIN_ROLE_ID");
    }
}
