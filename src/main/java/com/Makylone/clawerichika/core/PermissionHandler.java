package com.Makylone.clawerichika.core;

import com.Makylone.clawerichika.config.ConfigManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class PermissionHandler {

    private final String AdminIdRole;

    public PermissionHandler() {
        this.AdminIdRole = ConfigManager.GetAdminRoleId();
    }

    /**
     * Vérifie si l'utilisateur possède le rôle défini dans la config.
     */
    public boolean IsAdmin(Member member) {
        if (member == null) return false;
        for (Role role : member.getRoles()) {
            if (role.getId().equals(this.AdminIdRole)) {
                return true;
            }
        }
        return false;
    }
}
