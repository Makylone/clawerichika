package com.Makylone.clawerichika;

import com.Makylone.clawerichika.commands.boby.BobyCommand;
import com.Makylone.clawerichika.commands.ping.PingCommand;
import com.Makylone.clawerichika.config.ConfigManager;
import com.Makylone.clawerichika.core.CommandManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Main {

    public static void main(String[] args) {
        try {
            // 1. Initialisation du gestionnaire de commandes
            CommandManager commandManager = new CommandManager();

            commandManager.addCommand(new PingCommand());
            commandManager.addCommand(new BobyCommand());
            // 2. Création du bot
            JDA jda = JDABuilder.createDefault(ConfigManager.GetToken())
                .enableIntents(
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_MESSAGES
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setActivity(Activity.playing("Modérer le serveur"))
                .addEventListeners(commandManager) // On branche le listener
                .build();
        } catch (Exception e) {
            System.err.println("Erreur au démarrage du bot !");
            e.printStackTrace();
        }
    }
}
