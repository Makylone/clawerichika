package com.Makylone.clawerichika.commands.disconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

@ExtendWith(MockitoExtension.class)
class DisconnectCommandTest {

    @Mock SlashCommandInteractionEvent event;
    @Mock Guild guild;
    
    @Mock Member targetMember;
    @Mock Member selfMember;
    @Mock Member authorMember;
    
    @Mock User mockTargetUser;
    @Mock User mockSelfUser;
    @Mock User mockAuthorUser;

    // --- Mock pour le vocal ---
    @Mock GuildVoiceState targetVoiceState; // L'état vocal de la cible
    @Mock GuildVoiceState selfVoiceState;   // L'état vocal du bot
    @Mock InteractionHook hookAction;
    @Mock WebhookMessageCreateAction messageAction;
    @Mock ReplyCallbackAction replyAction;


    @Mock OptionMapping targetOption;
    @Mock AuditableRestAction disconnectAction; // L'action d'éjection du vocal

    DisconnectCommand command;

    @BeforeEach
    void setUp() {
        command = new DisconnectCommand();

        // 1. Liaison Membre -> User & IDs
        lenient().when(targetMember.getUser()).thenReturn(mockTargetUser);
        lenient().when(selfMember.getUser()).thenReturn(mockSelfUser);
        lenient().when(authorMember.getUser()).thenReturn(mockAuthorUser);
        
        lenient().when(mockTargetUser.getIdLong()).thenReturn(10000000L);
        lenient().when(mockSelfUser.getIdLong()).thenReturn(20000000L);
        lenient().when(mockAuthorUser.getIdLong()).thenReturn(30000000L);

        // 2. Environnement & Options
        lenient().when(event.getGuild()).thenReturn(guild);
        lenient().when(guild.getSelfMember()).thenReturn(selfMember);
        lenient().when(event.getMember()).thenReturn(authorMember);

        // Option "cible"
        lenient().when(event.getOption("cible")).thenReturn(targetOption); 
        lenient().when(targetOption.getAsMember()).thenReturn(targetMember);

        // 3. Réponses Discord
        lenient().when(event.reply(anyString())).thenReturn(replyAction);
        lenient().when(event.deferReply()).thenReturn(replyAction);
        lenient().when(replyAction.setEphemeral(true)).thenReturn(replyAction);
        lenient().when(event.getHook()).thenReturn(hookAction);
        lenient().when(hookAction.sendMessage(anyString())).thenReturn(messageAction); 

        lenient().when(replyAction.setEphemeral(true)).thenReturn(replyAction);
        
        // --- 4. CONFIGURATION VOCALE  ---
        
        // On lie le Member à son VoiceState
        lenient().when(selfMember.hasPermission(any(Permission.class))).thenReturn(true);
        lenient().when(targetMember.getVoiceState()).thenReturn(targetVoiceState);
        lenient().when(selfMember.getVoiceState()).thenReturn(selfVoiceState);

        // On configure l'action d'éjection (kickVoiceMember est une méthode de GUILD)
        lenient().when(guild.kickVoiceMember(targetMember)).thenReturn(disconnectAction);
        lenient().when(disconnectAction.reason(anyString())).thenReturn(disconnectAction);
    }

    @Test
    void shouldDisconnect_WhenMemberIsConnected() {
        // --- GIVEN ---
        // La cible EST connectée en vocal
        when(targetVoiceState.inAudioChannel()).thenReturn(true);
        
        // Permissions OK (Bot et Auteur peuvent interagir avec la cible)
        lenient().when(selfMember.canInteract(targetMember)).thenReturn(true);
        when(authorMember.canInteract(targetMember)).thenReturn(true);

        // --- WHEN ---
        command.execute(event);

        // --- THEN ---
        // Vérifie qu'on a bien appelé la méthode d'éjection sur l'objet guild
        verify(guild).kickVoiceMember(targetMember);
        verify(disconnectAction).queue(any(), any()); // Vérifie que l'action est partie
    }

    @Test
    void shouldNotDisconnect_WhenMemberIsNotConnected() {
        // --- GIVEN ---
        when(targetVoiceState.inAudioChannel()).thenReturn(false);

        // --- WHEN ---
        command.execute(event);

        // --- THEN ---
        // Vérifie le message d'erreur
        verify(hookAction).sendMessage(contains("pas en vocal"));
        
        // Vérifie qu'on n'a jamais essayé d'éjecter
        verify(guild, never()).kickVoiceMember(any(Member.class));
    }
}