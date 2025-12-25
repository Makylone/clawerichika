package com.Makylone.clawerichika.core;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.Makylone.clawerichika.commands.ICommand;
import com.Makylone.clawerichika.commands.ban.BanCommand;
import com.Makylone.clawerichika.commands.ban.UnbanCommand;
import com.Makylone.clawerichika.commands.boby.BobyCommand;
import com.Makylone.clawerichika.commands.disconnect.DisconnectCommand;
import com.Makylone.clawerichika.commands.ping.PingCommand;
import com.Makylone.clawerichika.commands.spinthewheel.SpinthwheelCommand;
import com.Makylone.clawerichika.commands.timeout.TimeoutCommand;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class CommandManager extends ListenerAdapter {

    private final List<ICommand> commands = new ArrayList<>();
    private final PermissionHandler PermissionHandler = new PermissionHandler();

    public CommandManager() {
        registerCommand();
    }

    public void addCommand(ICommand command) {
        commands.add(command);
    }

    private void registerCommand() {
        addCommand(new PingCommand());
        addCommand(new BanCommand());
        addCommand(new UnbanCommand());
        addCommand(new BobyCommand());
        addCommand(new TimeoutCommand());
        addCommand(new DisconnectCommand());
        addCommand(new SpinthwheelCommand());
        //addCommand(new PingCommand());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        List<CommandData> commandDataList = new ArrayList<>();

        // 1. On transforme tes classes ICommand en données Discord
        for (ICommand command : commands) {
            // On prépare la commande
            SlashCommandData data = Commands.slash(
                command.GetName(),
                command.GetDescription()
            );

            // On ajoute les options (paramètres)
            if (
                command.GetOptions() != null && !command.GetOptions().isEmpty()
            ) {
                data.addOptions(command.GetOptions());
            }

            // On définit si c'est admin only (optionnel, mais bonne pratique)
            // setDefaultPermissions permet de masquer la commande aux non-admins côté Discord
            /* if (command.isAdminOnly()) {
                 data.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
            } */

            commandDataList.add(data);
        }

        // 2. ENVOI MASSIF : On remplace toutes les commandes du bot par cette nouvelle liste
        event
            .getJDA()
            .updateCommands()
            .addCommands(commandDataList)
            .queue(
                success ->
                    System.out.println(
                        "✅ Commandes mises à jour avec succès : " +
                            success.size()
                    ),
                error ->
                    System.err.println(
                        "❌ Erreur lors de la mise à jour des commandes : " +
                            error.getMessage()
                    )
            );
    }

    @Override
    public void onSlashCommandInteraction(
        @NotNull SlashCommandInteractionEvent event
    ) {
        for (ICommand command : commands) {
            if (command.GetName().equals(event.getName())) {
                if (
                    command.IsAdminOnly() &&
                    !PermissionHandler.IsAdmin(event.getMember())
                ) {
                    event
                        .reply(
                            "⛔ Vous n'avez pas la permission d'utiliser cette commande (Bot Admin requis)."
                        )
                        .setEphemeral(true)
                        .queue();
                    return;
                }
                command.execute(event);
            }
        }
    }
}
