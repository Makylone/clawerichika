package com.Makylone.clawerichika.commands.ban;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.Makylone.clawerichika.commands.ICommand;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class BanCommand implements ICommand {

    @Override
    public String GetName() {
        return "bansleep";
    }

    @Override
    public String GetDescription() {
        return "Ban la personne pendant une durée donnée.";
    }

    @Override
    public boolean IsAdminOnly() {
        return true;
    }

    @Override
    public List<OptionData> GetOptions() {
        List<OptionData> options = new ArrayList<>();
        options.add(
            new OptionData(
                OptionType.USER,
                "personne",
                "La personne qui va être ban.",
                true
            )
        );
        options.add(
            new OptionData(
                OptionType.INTEGER,
                "durée",
                "La durée du ban (perma si aucun)",
                false
            )
        );
        return options;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply();
        // On regarde si la personne ne s'est pas auto selectionné
        Member member = event.getMember();
        Member targetMember = event.getOption("personne").getAsMember();
        // Si jamais la personne n'est pas sur le serveur
        User targetUser = event.getOption("personne").getAsUser();
        
        if (
            member.getUser().getIdLong() == targetMember.getUser().getIdLong()
        ) {
            System.out.println("Impossible d'autoban");
            event
                .reply(
                    "Ti est fou ou quoi ? Ti est pas un peu fada a vouloir te ban toi même, mon lion?"
                )
                .queue();
            return;
        }
        // Est ce que l'utilisateur veut ban le owner
        if (targetMember.isOwner()) {
            event
                .reply("non. tiens mange toi un timeout")
                .setEphemeral(true)
                .queue();
            member.timeoutFor(60, TimeUnit.SECONDS).queue();
            return;
        }
        // On regarde aussi si la personne peut ban la personne ciblée
        Member selfMember = event.getGuild().getSelfMember();
        System.out.println(selfMember.getUser().getName());
        // Est ce que le bot peut ban la personne visée?
        if (!selfMember.canInteract(targetMember)) {
            event
                .reply(
                    "Impossible de ban une personne ayant des rôles supérieures au bot."
                )
                .queue();
            return;
        }
        // Est ce que l'utilisateur a les droits pour ban la personne
        // (double garde fou par rapport à PermisionHandler qui
        // gère déjà que seuls les admins peuvent faire cette commande.)
        if (!member.canInteract(targetMember)) {
            event.reply("Ti es fou ?? Ti veux ban un frérot?").queue();
            return;
        }
        // On check si le targetUser n'est pas le ownership
        if (targetUser.getId().equals(event.getGuild().getOwnerId())) {
            event
                .getHook()
                .sendMessage(
                    "Vous ne pouvez pas bannir le propriétaire (même s'il n'est pas considéré comme membre)."
                )
                .queue();
            return;
        }
        if (targetMember != null) {
            event
                .getGuild()
                .ban(targetMember, 999999, TimeUnit.DAYS)
                .queue(
                    success -> {
                        event
                            .getHook()
                            .sendMessage(
                                "**Ban: ** " +
                                    targetUser.getAsMention() +
                                    " a été banni."
                            )
                            .queue();
                    },
                    error -> {
                        event
                            .getHook()
                            .sendMessage(
                                "❌ Erreur lors du ban : " + error.getMessage()
                            )
                            .queue();
                    }
                );
        } else {
            event
                .getGuild()
                .ban(targetUser, 999999, TimeUnit.DAYS)
                .queue(
                    success -> {
                        event
                            .getHook()
                            .sendMessage(
                                "**Ban: ** " +
                                    targetUser.getAsMention() +
                                    " a été banni."
                            )
                            .queue();
                    },
                    error -> {
                        event
                            .getHook()
                            .sendMessage(
                                "❌ Erreur lors du ban : " + error.getMessage()
                            )
                            .queue();
                    }
                );
        }
    }
}
