package com.Makylone.clawerichika.commands.timeout;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.Makylone.clawerichika.commands.ICommand;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class TimeoutCommand implements ICommand {

  public static final String OPT_DUREE = "duree";
  public static final String OPT_CIBLE = "cible";

  @Override
  public String GetName() {
    return "timeoutsleep";
  }

  @Override
  public String GetDescription() {
    return "Timeout la personne selectionnée";
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
        "cible",
        "La personne qui va se faire timeout",
        true
      )
    );
    options.add(
      new OptionData(
        OptionType.INTEGER,
        OPT_DUREE,
        "La durée du timeout (en seconde, ne peut pas exéder 2h)",
        true
      )
    );
    return options;
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    Member member = event.getMember();
    Member targetMember = event.getOption(OPT_CIBLE).getAsMember();

    System.out.println("ID Auteur : " + member.getUser().getIdLong());
    System.out.println("ID Cible  : " + targetMember.getUser().getIdLong());
    
    if (member.getUser().getIdLong() == targetMember.getUser().getIdLong()) {
      event.getHook().sendMessage("Impossible de s'auto-timeout").queue();
      return;
    }
    long duration = event.getOption(OPT_DUREE).getAsLong();

    System.out.println("Duration: " + duration);
    
    if (targetMember.isOwner()) {
      event.reply("?, aller hop mange toi le timeout").queue();
      member.timeoutFor(Duration.ofSeconds(duration)).queue();
      return;
    }
    if (!member.canInteract(targetMember)) {
      event
        .getHook()
        .sendMessage("Wsh tu peux pas le to, essaie de le boby avant pour voir")
        .queue();
      return;
    }
    targetMember.timeoutFor(Duration.ofSeconds(duration)).queue();
    event
      .getHook()
      .sendMessage(targetMember.getAsMention() + "a bien ete timeout")
      .queue();
  }
}
