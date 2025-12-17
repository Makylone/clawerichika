package com.Makylone.clawerichika.commands.ban;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent; 
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

@ExtendWith(MockitoExtension.class)
public class BanCommandTest {

    @Mock SlashCommandInteractionEvent event;
    @Mock Guild guild;
    
    @Mock Member targetMember;
    @Mock Member selfMember;
    @Mock Member authorMember;

    @Mock User mockTargetUser;
    @Mock User mockSelfUser;
    @Mock User mockAuthorUser;

    @Mock OptionMapping optionMapping;
    @Mock ReplyCallbackAction replyAction;
    @Mock AuditableRestAction banAction;

    BanCommand banCommand;

    @BeforeEach
    void setUp() {
        banCommand = new BanCommand();

        // 1. Liaison Membre -> User
        lenient().when(targetMember.getUser()).thenReturn(mockTargetUser);
        lenient().when(selfMember.getUser()).thenReturn(mockSelfUser);
        lenient().when(authorMember.getUser()).thenReturn(mockAuthorUser);

        // 2. Configuration des IDs
        lenient().when(mockTargetUser.getId()).thenReturn("1000");
        lenient().when(mockTargetUser.getIdLong()).thenReturn(1000L);
        lenient().when(mockSelfUser.getId()).thenReturn("2000");
        lenient().when(mockSelfUser.getIdLong()).thenReturn(2000L);
        lenient().when(mockAuthorUser.getId()).thenReturn("3000");
        lenient().when(mockAuthorUser.getIdLong()).thenReturn(3000L);

        // 3. Configuration de l'environnement
        lenient().when(event.getGuild()).thenReturn(guild);
        lenient().when(guild.getSelfMember()).thenReturn(selfMember);
        lenient().when(event.getMember()).thenReturn(authorMember);
        
        // 4. Configuration de l'Option
        lenient().when(event.getOption("personne")).thenReturn(optionMapping);
        lenient().when(optionMapping.getAsMember()).thenReturn(targetMember);
        lenient().when(optionMapping.getAsUser()).thenReturn(mockTargetUser); 

        // 5. Configuration de la réponse Discord
        lenient().when(event.deferReply()).thenReturn(replyAction);
        lenient().when(event.reply(anyString())).thenReturn(replyAction);
        lenient().when(replyAction.setEphemeral(true)).thenReturn(replyAction);
        
        // 6. Configuration des Actions
        
        // On utilise UserSnowflake.class au lieu de User.class
        lenient().when(guild.ban(any(UserSnowflake.class), anyInt(), any(TimeUnit.class))).thenReturn(banAction);
        lenient().when(banAction.reason(anyString())).thenReturn(banAction);

        // On garde member.ban au cas où
        lenient().when(targetMember.ban(anyInt(), any(TimeUnit.class))).thenReturn(banAction);

        // Mock du Timeout
        lenient().when(targetMember.timeoutFor(anyLong(), any(TimeUnit.class))).thenReturn(banAction);
        lenient().when(authorMember.timeoutFor(anyLong(), any(TimeUnit.class))).thenReturn(banAction);
    }

    @Test
    void shouldBanMember_WhenPermissionsAreValid() {
        // --- A. GIVEN ---
        when(selfMember.canInteract(targetMember)).thenReturn(true);
        when(authorMember.canInteract(targetMember)).thenReturn(true);
        when(targetMember.isOwner()).thenReturn(false);

        // --- B. WHEN ---
        banCommand.execute(event);

        // --- C. THEN ---
        // Vérifie que l'appel a bien eu lieu sur la Guild avec l'interface Snowflake
        verify(guild).ban(any(UserSnowflake.class), anyInt(), any(TimeUnit.class));
    }

    @Test
    void shouldNotBan_WhenTargetIsOwner() {
        // --- A. GIVEN ---
        lenient().when(selfMember.canInteract(targetMember)).thenReturn(true);
        lenient().when(authorMember.canInteract(targetMember)).thenReturn(true);
        
        when(targetMember.isOwner()).thenReturn(true);

        // --- B. WHEN ---
        banCommand.execute(event);

        // --- C. THEN ---
        verify(event).reply(contains("non")); 
        
        // On vérifie que le ban n'a jamais été appelé
        verify(guild, never()).ban(any(UserSnowflake.class), anyInt(), any(TimeUnit.class));
    }
}