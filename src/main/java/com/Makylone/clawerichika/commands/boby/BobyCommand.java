package com.Makylone.clawerichika.commands.boby;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.Makylone.clawerichika.commands.ICommand;
import com.Makylone.clawerichika.config.ConfigManager;
import com.Makylone.clawerichika.utils.CooldownManager;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class BobyCommand implements ICommand {

  // ID Admin
  private final String AdminIdrole;

  // Cooldown de 30 heures, convertit en millisecondes
  private final long COOLDOWN_DURATION = TimeUnit.HOURS.toMillis(30);

  private final CooldownManager cooldownManager = new CooldownManager(
    "boby_command_last_execution"
  );

  public BobyCommand() {
    this.AdminIdrole = ConfigManager.GetAdminRoleId();
  }

  @Override
  public String GetName() {
    return "boby";
  }

  @Override
  public String GetDescription() {
    return "Donne le rôle 'boby' à une personne et le retire aux autres.";
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
        "La personne qui va recevoir le rôle",
        true
      )
    );
    return options;
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    // On regarde si un cooldown est appliqué ou non à la commande:
    long lastExution = cooldownManager.loadLastExecution();
    long currentTimestamp = System.currentTimeMillis();
    long nextAvailableTime = lastExution + COOLDOWN_DURATION;
    // On regarde la différence entre le timestamp actuel et le timestamp de la prochaine disponibilité de la commande
    if (currentTimestamp < nextAvailableTime) {
      long secondsRemaining = (nextAvailableTime) / 1000;
      event
        .reply(
          "Attention, la commande boby ne sera disponible que <t:" +
          secondsRemaining +
          ":R>"
        )
        .setEphemeral(false)
        .queue();
      return;
    }
    // On diffère la réponse car parcourir les membres peut prendre > 3 secondes
    event.deferReply().queue();

    Member targetMember = event.getOption("cible").getAsMember();

    // ID du role Boby (Idéalement à mettre dans le ConfigManager aussi)
    Role bobyRole = event.getGuild().getRoleById("1407656913503522836");

    Role adminRole = event.getGuild().getRoleById(this.AdminIdrole);

    if (targetMember == null) {
      event.getHook().sendMessage("Impossible de trouver ce membre.").queue();
      return;
    }

    if (bobyRole == null) {
      event
        .getHook()
        .sendMessage("Le rôle boby est introuvable (mauvais ID ?).")
        .queue();
      return;
    }
    // On va aussi regarder si la personne ciblé est admin ou non (seul les admins peuvent avoir le rôle boby)
    List<Role> rolesOfTheTargetMember = targetMember.getRoles();
    if (
      !rolesOfTheTargetMember.contains(adminRole) &&
      !rolesOfTheTargetMember.contains(bobyRole)
    ) {
      event
        .getHook()
        .sendMessage(
          "Vous ne pouvez pas mettre le rôle boby sur une personne non admin."
        )
        .queue();
      return;
    }
    // findMemberWithRole au lieu de getMembersWithRoles pour interroger directement l'API discord
    event
      .getGuild()
      .findMembersWithRoles(bobyRole)
      .onSuccess(membersWithBoby -> {
        System.out.println("Membres trouvés : " + membersWithBoby.size());

        // 1. On retire le rôle boby aux personnes ayant déjà le rôle.
        for (Member oldBoby : membersWithBoby) {
          // On va d'abord regarder si la personne à qui ont veut attribuer le rôle boby ne l'a déjà pas
          if (oldBoby.getId().equals(targetMember.getId())) {
            event
              .getHook()
              .sendMessage("Cette personne a déjà le rôle boby")
              .queue();
            return;
          }
          event.getGuild().removeRoleFromMember(oldBoby, bobyRole).queue();
          // On redonne aussi le rôle admin aux anciens boby
          event.getGuild().addRoleToMember(oldBoby, adminRole).queue();
          System.out.println(
            oldBoby.getUser().getName() + " a récupéré ses rôles"
          );
        }
        // 2. On retire a retire aussi le rôle d'admin à la personne qui va recevoir le rôle boby
        event.getGuild().removeRoleFromMember(targetMember, adminRole).queue();
        System.out.println(
          "Rôle admin retirer à: " + targetMember.getUser().getName()
        );

        // 3. On donne le rôle à l'utilisateur ciblé
        event
          .getGuild()
          .addRoleToMember(targetMember, bobyRole)
          .queue(
            success -> {
              event
                .getHook()
                .sendMessage(
                  "Le rôle boby a été transféré à " +
                  targetMember.getAsMention()
                )
                .queue();
              cooldownManager.saveLastExecutionTime();
            },
            failure -> {
              event
                .getHook()
                .sendMessage("Erreur : Impossible de donner le rôle.")
                .queue();
            }
          );
      })
      .onError(error -> {
        event
          .getHook()
          .sendMessage("Erreur technique lors de la recherche des membres.")
          .queue();
        error.printStackTrace();
      });
  }
}
