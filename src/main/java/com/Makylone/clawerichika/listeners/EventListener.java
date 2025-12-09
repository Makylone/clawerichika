package com.Makylone.clawerichika.listeners;

import java.awt.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class EventListener extends ListenerAdapter {

    /**
     * Événement déclenché quand un nouvel utilisateur rejoint le serveur.
     */
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        // 1. On récupère le salon système (souvent "général" ou "bienvenue")
        // Note : Assure-toi que ce salon est configuré dans les paramètres de ton serveur Discord
        TextChannel welcomeChannel = event.getGuild().getSystemChannel();

        // Si le salon système existe et que le bot peut écrire dedans
        if (welcomeChannel != null && welcomeChannel.canTalk()) {
            // On crée un joli Embed de bienvenue
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Bienvenue " + event.getUser().getName() + " !");
            embed.setDescription(
                "Nous sommes ravis de te voir sur " +
                    event.getGuild().getName() +
                    "."
            );
            embed.setThumbnail(event.getUser().getAvatarUrl()); // Photo de profil du nouveau venu
            embed.setColor(Color.CYAN);
            embed.setFooter("ID Membre: " + event.getMember().getId());

            // On envoie le message
            welcomeChannel.sendMessageEmbeds(embed.build()).queue();

            // On peut aussi lui ajouter un rôle automatiquement ici (ex: "Membre")
            // event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById("ID_DU_ROLE")).queue();
        }
    }

    /**
     * Événement déclenché à CHAQUE message envoyé sur le serveur.
     * Utile pour de la modération automatique ou des "easter eggs".
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // RÈGLE D'OR : Toujours ignorer les messages des bots (pour éviter les boucles infinies)
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw();

        // Exemple simple : Si quelqu'un dit "bonjour bot", on répond
        if (message.equalsIgnoreCase("bonjour bot")) {
            event.getChannel().sendMessage("Bonjour humain ! 👋").queue();
        }

        // Exemple modération : Supprimer les liens (très basique)
        if (message.contains("http://") || message.contains("https://")) {
            // Tu pourrais vérifier ici si l'auteur est admin via ton PermissionHandler avant de supprimer
            // event.getMessage().delete().queue();
            // event.getChannel().sendMessage("Les liens sont interdits !").queue();
        }
    }
}
