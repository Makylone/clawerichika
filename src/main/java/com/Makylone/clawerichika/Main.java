package com.Makylone.clawerichika;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Makylone.clawerichika.config.ConfigManager;
import com.Makylone.clawerichika.core.CommandManager;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        try {
            // 1. Initialisation du gestionnaire de commandes
            CommandManager commandManager = new CommandManager();
            logger.info("Démarrage du bot");
            // 2. Création du bot
            JDABuilder.createDefault(ConfigManager.GetToken())
                .enableIntents(
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_MESSAGES
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setActivity(Activity.playing("boop bip loser"))
                .addEventListeners(commandManager)
                .build();
            logger.debug("Build finit");
        } catch (Exception e) {
            System.err.println("Erreur au démarrage du bot !");
            e.printStackTrace();
        }
    }
}
