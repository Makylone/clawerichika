package com.Makylone.clawerichika.commands.ban;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

@ExtendWith(MockitoExtension.class)
public class UnbanCommandTest {
    @Mock SlashCommandInteractionEvent event;
    @Mock Guild guild;
    
    // Pour l'auteur et le bot, on a des Members
    @Mock Member selfMember;
    @Mock Member authorMember;
    @Mock User mockSelfUser;
    @Mock User mockAuthorUser;

    // Pour la cible (Bannie), on a seulement un User
    // Etant donner que la personne n'est plus un member
    @Mock User mockTargetUser; 

    @Mock OptionMapping targetOption;
    
    // Gestion des réponses (DeferReply / Hook)
    @Mock ReplyCallbackAction replyAction;
    @Mock InteractionHook hookAction;
    @Mock WebhookMessageCreateAction messageAction;
    
    @Mock AuditableRestAction unbanAction; 

    UnbanCommand command;

    @BeforeEach
    void setUp() {
        command = new UnbanCommand();

        // 1. Liaison Auteur/Bot (Member -> User -> ID)
        lenient().when(selfMember.getUser()).thenReturn(mockSelfUser);
        lenient().when(authorMember.getUser()).thenReturn(mockAuthorUser);
        
        lenient().when(mockSelfUser.getIdLong()).thenReturn(200L);
        lenient().when(mockAuthorUser.getIdLong()).thenReturn(300L);
        
        // Configuration de la cible (User uniquement)
        lenient().when(mockTargetUser.getIdLong()).thenReturn(100L);
        lenient().when(mockTargetUser.getAsMention()).thenReturn("<@100>");

        // 2. Environnement
        lenient().when(event.getGuild()).thenReturn(guild);
        lenient().when(guild.getSelfMember()).thenReturn(selfMember);
        lenient().when(event.getMember()).thenReturn(authorMember);

        // 3. Option "personne"
        lenient().when(event.getOption("personne")).thenReturn(targetOption); 
        
        // IMPORTANT : On récupère un user, pas un Member
        lenient().when(targetOption.getAsUser()).thenReturn(mockTargetUser);

        // 4. Réponses Discord
        lenient().when(event.deferReply()).thenReturn(replyAction);
        
        lenient().when(event.getHook()).thenReturn(hookAction);
        lenient().when(hookAction.sendMessage(anyString())).thenReturn(messageAction);
        
        // 5. Action Unban (JDA 5 utilise UserSnowflake)
        lenient().when(guild.unban(any(UserSnowflake.class))).thenReturn(unbanAction);
        lenient().when(unbanAction.reason(anyString())).thenReturn(unbanAction);
        // On dit : "Quand queue(success, error) est appelé..."
        doAnswer(invocation -> {
            // 1. On récupère le premier argument (le callback de succès)
            java.util.function.Consumer<Void> successCallback = invocation.getArgument(0);
            
            // 2. On l'exécute manuellement (ce qui lance ton code avec le sendMessage)
            // On passe 'null' car unban ne renvoie rien de particulier (Void)
            successCallback.accept(null); 
            
            return null;
        }).when(unbanAction).queue(any(), any());
        lenient().when(selfMember.hasPermission(any(Permission.class))).thenReturn(true);
    }   

    @Test
    void shouldUnban_WhenUserIsValid() {
        // --- GIVEN ---
        // Tout est déjà configuré dans le setUp pour le cas nominal

        // --- WHEN ---
        command.execute(event);

        // --- THEN ---
        // 1. On vérifie que la guilde a bien appelé unban sur notre User cible
        verify(guild).unban(mockTargetUser);
        
        // 2. On vérifie que l'action est partie
        verify(unbanAction).queue(any(), any());
        
        // 3. On vérifie le message de confirmation
        verify(hookAction).sendMessage(contains("débanni")); // Adapte le texte
    }
}
