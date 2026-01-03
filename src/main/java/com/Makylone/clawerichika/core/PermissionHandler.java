package com.Makylone.clawerichika.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Makylone.clawerichika.config.ConfigManager;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class PermissionHandler {

    private final String AdminIdRole;
    private static final Logger logger = LoggerFactory.getLogger(PermissionHandler.class);

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
                logger.warn(member.getNickname() + " a executé une commande d'admin");
                return true;
            }
        }
        logger.warn(member.getNickname() + " a essayé d'executer une commande d'admin sans les droits");
        return false;
    }
}
