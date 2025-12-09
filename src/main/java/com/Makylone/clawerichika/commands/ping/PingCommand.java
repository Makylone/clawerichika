package com.Makylone.clawerichika.commands.ping;

import com.Makylone.clawerichika.commands.ICommand;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class PingCommand implements ICommand {

    @Override
    public String GetName() {
        return "ping";
    }

    @Override
    public String GetDescription() {
        return "Répond pong";
    }

    @Override
    public boolean IsAdminOnly() {
        return false;
    }

    @Override
    public List<OptionData> GetOptions() {
        return new ArrayList<>();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long time = System.currentTimeMillis();

        event
            .reply("Pong!")
            .setEphemeral(false)
            .queue(reponse -> {
                long lantency = System.currentTimeMillis() - time;
                reponse.editOriginal("Pong! avec " + lantency + " ms").queue();
            });
    }
}
