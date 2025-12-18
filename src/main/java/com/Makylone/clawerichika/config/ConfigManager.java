package com.Makylone.clawerichika.config;

import io.github.cdimascio.dotenv.Dotenv;

public class ConfigManager {

    private static final Dotenv dotenv = Dotenv.load();

    public static String GetToken() {
        if(!dotenv.get("TOKEN_DISCORD").isEmpty() && dotenv.get("TOKEN_DISCORD") != null){
            return dotenv.get("TOKEN_DISCORD");
        } else {
            return System.getenv("TOKEN_DISCORD");
        }  
    }

    public static String GetAdminRoleId() {
        if(!dotenv.get("ADMIN_ROLE_ID").isEmpty() && dotenv.get("ADMIN_ROLE_ID") != null){
            return dotenv.get("ADMIN_ROLE_ID");
        } else {
            return System.getenv("ADMIN_ROLE_ID");
        }
    }
}
