package com.Makylone.clawerichika;

import com.Makylone.clawerichika.config.ConfigManager;
import com.Makylone.clawerichika.core.CommandManager;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Main {

    public static void main(String[] args) {
        try {
            // 1. Initialisation du gestionnaire de commandes
            CommandManager commandManager = new CommandManager();

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
        } catch (Exception e) {
            System.err.println("Erreur au démarrage du bot !");
            e.printStackTrace();
        }
    }
}
