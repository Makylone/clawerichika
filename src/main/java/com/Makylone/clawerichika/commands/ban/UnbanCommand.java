package com.Makylone.clawerichika.commands.ban;

import java.util.ArrayList;
import java.util.List;

import com.Makylone.clawerichika.commands.ICommand;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class UnbanCommand implements ICommand {

    @Override
    public String GetName() {
        return "unbansleep";
    }

    @Override
    public String GetDescription() {
        return "Unban une personne";
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
                "La personne qui va être unban.",
                true
            )
        );
        return options;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        // Check si le bot a les permissions pour unban
        if (
            !event
                .getGuild()
                .getSelfMember()
                .hasPermission(Permission.BAN_MEMBERS)
        ) {
            event
                .getHook()
                .sendMessage(
                    "Le bot n'a pas les permissions pour unban (ou ban) la personne"
                )
                .queue();
            return;
        }
        User userToUnban = event.getOption("personne").getAsUser();
        event
            .getGuild()
            .unban(userToUnban)
            .queue(
                success -> {
                    // SUCCÈS : La personne était bien bannie et a été retirée de la liste noire
                    event
                        .getHook()
                        .sendMessage(
                            userToUnban.getName() +
                                " (" +
                                userToUnban.getId() +
                                ") a été débanni."
                        )
                        .queue();
                },
                error -> {
                    // ERREUR : Souvent, c'est parce que l'utilisateur N'ÉTAIT PAS banni.
                    event
                        .getHook()
                        .sendMessage(
                            "Impossible de débannir cet utilisateur.\n" +
                                "Est-il vraiment banni ? (Erreur : " +
                                error.getMessage() +
                                ")"
                        )
                        .queue();
                }
            );
    }
}
