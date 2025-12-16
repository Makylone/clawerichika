package com.Makylone.clawerichika.commands.timeout;

import java.time.Duration; // <--- Vérifie que c'est bien java.time

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

@ExtendWith(MockitoExtension.class)
public class TimeoutCommandTest {

    @Mock SlashCommandInteractionEvent event;
    @Mock Guild guildId;
    
    @Mock Member targetMember;
    @Mock Member selfMember;
    @Mock Member authorMember;
    @Mock User mockTargetMember;
    @Mock User mockSelfMember;
    @Mock User mockAuthorMember;

    @Mock OptionMapping targetOption;
    @Mock OptionMapping durationOption;

    @Mock InteractionHook hookAction;
    @Mock WebhookMessageCreateAction messageAction;
    @Mock ReplyCallbackAction replyAction;
    @Mock AuditableRestAction timeoutAction;

    TimeoutCommand timeoutCommand;

    @BeforeEach
    void setUp(){
        timeoutCommand = new TimeoutCommand();

        // 1. Liaison Membre -> User
        lenient().when(targetMember.getUser()).thenReturn(mockTargetMember);
        lenient().when(selfMember.getUser()).thenReturn(mockSelfMember);
        lenient().when(authorMember.getUser()).thenReturn(mockAuthorMember);

        // 2. Configuration des IDs (Distincts pour éviter le return précoce)
        lenient().when(mockTargetMember.getIdLong()).thenReturn(10000L);
        lenient().when(mockSelfMember.getIdLong()).thenReturn(20000L);
        lenient().when(mockAuthorMember.getIdLong()).thenReturn(30000L);

        // 3. Configuration de l'environnement
        lenient().when(event.getGuild()).thenReturn(guildId);
        lenient().when(guildId.getSelfMember()).thenReturn(selfMember);
        lenient().when(event.getMember()).thenReturn(authorMember);

        // 4. Configuration de l'Option 
        lenient().when(event.getOption("cible")).thenReturn(targetOption);
        lenient().when(event.getOption(TimeoutCommand.OPT_DUREE)).thenReturn(durationOption);
        
        lenient().when(targetOption.getAsMember()).thenReturn(targetMember);
        
        // 5. Configuration de la réponse Discord
        lenient().when(event.reply(anyString())).thenReturn(replyAction);
        lenient().when(event.deferReply()).thenReturn(replyAction); // Pour le deferReply()
        lenient().when(replyAction.setEphemeral(true)).thenReturn(replyAction);
        lenient().when(event.getHook()).thenReturn(hookAction);
        lenient().when(hookAction.sendMessage(anyString())).thenReturn(messageAction); 

        // 6. Configuration des actions
        // On mocke la méthode qui prend une Duration (JDA 5 standard)
        lenient().when(targetMember.timeoutFor(any(Duration.class))).thenReturn(timeoutAction);
        lenient().when(authorMember.timeoutFor(any(Duration.class))).thenReturn(timeoutAction);
    }

    @Test
    void shouldTimeout_WhenEverythingIsOk(){
        // --- A. GIVEN ---
        // On utilise lenient() ici pour forcer Mockito à accepter ce stub
        // même s'il pense (à tort) qu'il est inutile.
        lenient().when(durationOption.getAsLong()).thenReturn(60L);

        // Permissions OK
        lenient().when(selfMember.canInteract(targetMember)).thenReturn(true);
        when(authorMember.canInteract(targetMember)).thenReturn(true);
        when(targetMember.isOwner()).thenReturn(false);

        // --- B. WHEN ---
        timeoutCommand.execute(event);

        // --- C. THEN --- 
        // 1. On vérifie que le timeout est appelé avec la bonne Duration
        verify(targetMember).timeoutFor(Duration.ofSeconds(60));
        
        // 2. On vérifie que l'action est envoyée
        verify(timeoutAction).queue();
    }

    @Test
    void shouldNotTimeout_WhenTargetMemberIsOwner(){
        // --- A. GIVEN ---
        lenient().when(durationOption.getAsLong()).thenReturn(60L);

        // Le targetMember est propriétaire du server
        lenient().when(selfMember.canInteract(targetMember)).thenReturn(true);
        lenient().when(authorMember.canInteract(targetMember)).thenReturn(true);
        when(targetMember.isOwner()).thenReturn(true);

        // --- B. WHEN ---
        timeoutCommand.execute(event);

        // --- C. THEN ---
        // 1. On regarde si la réponse a bien été envoyé
        verify(event).reply(contains("mange toi le timeout"));

        // 2. On vérifie que le timeout a bien été appliqué sur le membre
        // Ayant fait la commande
        verify(authorMember).timeoutFor(Duration.ofSeconds(60));

        // 3. On regarde si l'action a bien été envoyée
        verify(timeoutAction).queue();
    }

    @Test
    void shouldNotTimeout_WhenAuthorMemberCantInteractWithTargetMember(){
         // --- A. GIVEN ---
        lenient().when(durationOption.getAsLong()).thenReturn(60L);

        // Le targetMember est propriétaire du server
        lenient().when(selfMember.canInteract(targetMember)).thenReturn(true);
        lenient().when(authorMember.canInteract(targetMember)).thenReturn(false);
        when(targetMember.isOwner()).thenReturn(false);

        // --- B. WHEN ---
        timeoutCommand.execute(event);

        // --- C. THEN ---
        // 1. On regarde si le message a bien été envoyé
        verify(hookAction).sendMessage(contains("Wsh tu peux pas le to"));

    }
}