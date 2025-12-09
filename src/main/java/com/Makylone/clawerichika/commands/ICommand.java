package com.Makylone.clawerichika.commands;

import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public interface ICommand {
    String GetName(); // Nom de la commande
    String GetDescription(); // Description de la commande pour discord
    boolean IsAdminOnly(); // Check si la commande est uniquement pour les administrateurs

    List<OptionData> GetOptions(); // Avoir les paramètres de la commandes, comme par exemple la personne ect...

    void execute(SlashCommandInteractionEvent event);
}
