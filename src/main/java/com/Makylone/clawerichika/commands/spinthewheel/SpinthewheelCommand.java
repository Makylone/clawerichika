package com.Makylone.clawerichika.commands.spinthewheel;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Makylone.clawerichika.commands.ICommand;
import com.Makylone.clawerichika.utils.AnimatedWheelGenerator;
import com.Makylone.clawerichika.utils.CooldownManager;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;

public class SpinthewheelCommand implements ICommand{

    private final long COOLDOWN_DURATION = TimeUnit.DAYS.toMillis(1);
    private static final Logger logger = LoggerFactory.getLogger(SpinthewheelCommand.class);
    private final CooldownManager cooldownManager = new CooldownManager(
    "spinthewheel_command_last_execution"
    );

    @Override
    public String GetName() {
        return "spindawheel";
    }

    @Override
    public String GetDescription() {
       return "Fait tourner la roue";
    }

    @Override
    public boolean IsAdminOnly() {
        return false;
    }

    @Override
    public List<OptionData> GetOptions() {
        List<OptionData> options = new ArrayList<>();
        options.add(
            new OptionData(
                OptionType.USER, 
                "cible", 
                "La personne qui va subir la roue.",
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
        logger.info("lastExecution: " + lastExution + " currentTimestamp: " + currentTimestamp + " nextAvailableTime: " + nextAvailableTime);
        // On regarde la différence entre le timestamp actuel et le timestamp de la prochaine disponibilité de la commande
        if (currentTimestamp < nextAvailableTime) {
        long secondsRemaining = (nextAvailableTime) / 1000;
            event
                .reply(
                    "Attention, la commande spin the wheel ne sera disponible que <t:" +
                    secondsRemaining +
                    ":R>"
                )
                .setEphemeral(false)
                .queue();
            return;
        }
        event.deferReply().queue();
        Member targetMember = event.getOption("cible").getAsMember();
        logger.debug("target member: " + targetMember.getNickname());
        if(targetMember == null) {
            event.getHook().sendMessage("Membre introuvable !").queue();
            return;
        }

        // Lancer la séquence
        runWheelSequence(event, targetMember, false);
        cooldownManager.saveLastExecutionTime();
    }

    private void runWheelSequence(SlashCommandInteractionEvent event, Member victim, boolean isReversed){
        Random random = new Random();
        int winnerIndex = random.nextInt(AnimatedWheelGenerator.OPTIONS.length);
        String winnerName = AnimatedWheelGenerator.OPTIONS[winnerIndex];
        logger.debug(winnerName);
        // 1. Générer et Envoyer le GIF (Asynchrone)
        CompletableFuture.supplyAsync(() -> {
            try {
                return AnimatedWheelGenerator.generateGifWithFFmpeg(winnerIndex);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(gifBytes -> {
            // 2. Une fois le GIF généré, on l'envoie
            event.getHook().sendFiles(FileUpload.fromData(gifBytes, "roue.gif"))
                 .setContent((isReversed ? "↩️ REVERSE !" : "La roue a parlé : ") + "**" + winnerName + "** !")
                 .queue(success -> {
                     // 3. Après l'envoie, on punit (Délai optionnel pour le suspense)
                     applyPunishment(event, victim, winnerIndex, isReversed);
                 });
        }).exceptionally(ex -> {
            event.getHook().sendMessage("Erreur lors de la génération de la roue.").queue();
            ex.printStackTrace();
            return null;
        });
    }


    protected void applyPunishment(SlashCommandInteractionEvent event, Member victim, int winnerIndex, boolean isReversed) {
        logger.debug(""+winnerIndex);
        logger.debug("victim: " + victim.getNickname());
        logger.debug("applyPunishment have been called");
        switch (winnerIndex) {
            case 0 -> {
                event.getHook().sendMessage("5 minutes de TO pour " + victim.getAsMention()).queue();
                logger.debug("TO 5min");
                victim.timeoutFor(Duration.ofMinutes(5)).queue();
            }
            case 1 -> {
                event.getHook().sendMessage("10 minutes de TO pour " + victim.getAsMention()).queue();
                logger.debug("TO 10min");
                victim.timeoutFor(Duration.ofMinutes(10)).queue();
            }
            case 2 -> {
                event.getHook().sendMessage("1h de TO pour " + victim.getAsMention()).queue();
                logger.debug("TO 1h");
                victim.timeoutFor(Duration.ofHours(1)).queue();
            }
            case 3 -> {
                logger.debug("reset roles");
                List<Role> roles = victim.getRoles();
                for (Role role : roles) {
                    if (!role.getId().equals("1361670311787106334")) {
                         event.getGuild().removeRoleFromMember(victim, role).queue();
                    }
                }
                event.getHook().sendMessage("Cheh ! Plus de rôles pour " + victim.getAsMention()).queue();
            }
            case 4 -> { 
                event.getHook().sendMessage("REVERSE ! Attention au retour de flamme 🔥🔥🔥").queue();
                logger.debug("reverse");
                if(isReversed){
                    runWheelSequence(event, victim, false);
                }
                // On relance la roue sur l'auteur de la commande
                Member author = event.getMember();
                
                runWheelSequence(event, author, true);
            }
        }
    }
}
