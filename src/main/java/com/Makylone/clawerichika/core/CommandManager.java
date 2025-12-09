package com.Makylone.clawerichika.core;

import com.Makylone.clawerichika.commands.ICommand;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CommandManager extends ListenerAdapter {

    private final List<ICommand> Commands = new ArrayList<>();
    private final PermissionHandler PermissionHandler = new PermissionHandler();

    public void addCommand(ICommand command) {
        Commands.add(command);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        for (ICommand command : Commands) {
            event
                .getJDA()
                .upsertCommand(command.GetName(), command.GetDescription())
                .addOptions(command.GetOptions())
                .queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(
        @NotNull SlashCommandInteractionEvent event
    ) {
        for (ICommand command : Commands) {
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
