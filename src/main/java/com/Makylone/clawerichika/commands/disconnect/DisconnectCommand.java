package com.Makylone.clawerichika.commands.disconnect;

import java.util.ArrayList;
import java.util.List;

import com.Makylone.clawerichika.commands.ICommand;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class DisconnectCommand implements ICommand {

  @Override
  public String GetName() {
    return "disconnect";
  }

  @Override
  public String GetDescription() {
    return "Déconnecte la personne du vocal";
  }

  @Override
  public boolean IsAdminOnly() {
    return true;
  }

  @Override
  public List<OptionData> GetOptions() {
    List<OptionData> options = new ArrayList<>();
    options.add(
      new OptionData(OptionType.USER, "cible", "La personne à déconnecter")
    );
    return options;
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    event.deferReply();

    Member member = event.getMember();
    Member bot = event.getGuild().getSelfMember();
    Member targetMember = event.getOption("cible").getAsMember();

    if (targetMember == null) {
      event.getHook().sendMessage("Le membre est introuvable").queue();
      return;
    }

    GuildVoiceState voiceState = targetMember.getVoiceState();
    if (voiceState == null || !voiceState.inAudioChannel()) {
      event
        .getHook()
        .sendMessage(
          "La personne que vous tenter de déconencter n'est acutuellement pas en vocal."
        )
        .queue();
      return;
    }

    if (!bot.hasPermission(Permission.VOICE_MOVE_OTHERS)) {
      event
        .getHook()
        .sendMessage(
          "Le bot n'a pas les droits suffisant pour déconnecter une personne."
        )
        .queue();
      return;
    }
    if (!member.canInteract(targetMember)) {
      event
        .getHook()
        .sendMessage(
          "Vous n'avez pas les droits nécessaire pour déconnecter cette personne"
        )
        .queue();
      return;
    }

    event
      .getGuild()
      .kickVoiceMember(targetMember)
      .queue(
        sucess -> {
          event
            .reply(targetMember.getAsMention() + " a bien été déconnecté")
            .setEphemeral(true)
            .queue();
        },
        error -> {
          event
            .reply("Erreur lors de la déconnection du membre.")
            .setEphemeral(true)
            .queue();
        }
      );
  }
}
