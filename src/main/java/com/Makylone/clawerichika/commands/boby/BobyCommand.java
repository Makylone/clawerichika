package com.Makylone.clawerichika.commands.boby;

import com.Makylone.clawerichika.commands.ICommand;
import com.Makylone.clawerichika.config.ConfigManager;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class BobyCommand implements ICommand {

    private final String AdminIdrole;

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
        // On diffère la réponse car parcourir les membres peut prendre > 3 secondes
        event.deferReply().queue();

        Member targetMember = event.getOption("cible").getAsMember();

        // ID du role Boby (Idéalement à mettre dans le ConfigManager aussi !)
        Role bobyRole = event.getGuild().getRoleById("1407656913503522836");

        Role adminRole = event.getGuild().getRoleById(this.AdminIdrole);

        if (targetMember == null) {
            event
                .getHook()
                .sendMessage("Impossible de trouver ce membre.")
                .queue();
            return;
        }

        if (bobyRole == null) {
            event
                .getHook()
                .sendMessage("Le rôle boby est introuvable (mauvais ID ?).")
                .queue();
            return;
        }

        // Récupérer la liste (Nécessite GUILD_MEMBERS Intent)
        List<Member> membersWithBoby = event
            .getGuild()
            .getMembersWithRoles(bobyRole);

        System.out.println(
            "Membres avec le rôle trouvés : " + membersWithBoby.size()
        );

        // 1. On retire le rôle à TOUT LE MONDE qui l'a actuellement
        for (Member oldBoby : membersWithBoby) {
            // Si la cible l'a déjà, on ne fait rien pour l'instant (on le gère après) ou on stop
            if (oldBoby.getId().equals(targetMember.getId())) {
                event
                    .getHook()
                    .sendMessage("Cette personne a déjà le rôle boby !")
                    .queue();
                return;
            }

            // On retire le rôle aux anciens
            event.getGuild().removeRoleFromMember(oldBoby, bobyRole).queue();
            System.out.println(
                "Rôle retiré à : " + oldBoby.getUser().getName()
            );
            // On remet le rôle admin aux anciens
            event.getGuild().addRoleToMember(oldBoby, adminRole).queue();
            System.out.println(
                "Rôle admin réattribuer à : " + oldBoby.getUser().getName()
            );
        }
        // 2. On retire le rôle admin
        event
            .getGuild()
            .removeRoleFromMember(targetMember, adminRole)
            .queue(
                success -> System.out.println("Rôle admin retirer"),
                failure ->
                    event
                        .getHook()
                        .sendMessage(
                            "Erreur: impossible de retirer le rôle admin."
                        )
            );
        // 3. On donne le rôle au nouveau
        event
            .getGuild()
            .addRoleToMember(targetMember, bobyRole)
            .queue(
                success ->
                    event
                        .getHook()
                        .sendMessage(
                            "Le rôle boby a été transféré à " +
                                targetMember.getAsMention()
                        )
                        .queue(),
                error ->
                    event
                        .getHook()
                        .sendMessage("Erreur : Impossible de donner le rôle.")
                        .queue()
            );
    }
}
