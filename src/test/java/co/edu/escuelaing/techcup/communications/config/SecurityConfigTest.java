package co.edu.escuelaing.techcup.communications.config;

import co.edu.escuelaing.techcup.communications.dto.ResolveReportRequest;
import co.edu.escuelaing.techcup.communications.entity.enums.ModeratorActionType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.entity.enums.ReportStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the real filter chain: authentication comes from the token alone and
 * moderation endpoints additionally require a moderator or organizer role.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID userId = UUID.randomUUID();

    private String bearerFor(String... roles) {
        return JwtTestTokens.bearer(JwtTestTokens.valid(userId, "alice", List.of(roles)));
    }

    private String resolveBody() throws Exception {
        return objectMapper.writeValueAsString(new ResolveReportRequest(
                ReportStatus.DISMISSED, "not offensive", ModeratorActionType.WARN));
    }

    @Test
    void rejectsARequestWithoutAToken() throws Exception {
        mockMvc.perform(get("/chats/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsARequestWithAnUnparsableToken() throws Exception {
        mockMvc.perform(get("/chats/{id}", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsARequestWithAnExpiredToken() throws Exception {
        mockMvc.perform(get("/chats/{id}", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearer(JwtTestTokens.expired(userId))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void letsAnAuthenticatedCallerReachTheHandler() throws Exception {
        // The chat does not exist, so reaching the handler surfaces as 404 rather than 401.
        mockMvc.perform(get("/chats/{id}", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(ParticipantRole.MEMBER.name())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deniesReportResolutionToPlainMembers() throws Exception {
        mockMvc.perform(post("/reports/{id}/resolve", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(ParticipantRole.MEMBER.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resolveBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsReportResolutionToModerators() throws Exception {
        mockMvc.perform(post("/reports/{id}/resolve", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(ParticipantRole.MODERATOR.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resolveBody()))
                .andExpect(status().isNotFound());
    }

    @Test
    void allowsReportResolutionToIdentityAdmins() throws Exception {
        // cc-identity-service has no MODERATOR role; ADMIN is its equivalent administrative role.
        mockMvc.perform(post("/reports/{id}/resolve", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, bearerFor("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resolveBody()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deniesFaqManagementToPlainMembers() throws Exception {
        mockMvc.perform(get("/faqs")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(ParticipantRole.MEMBER.name())))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsFaqManagementToModerators() throws Exception {
        mockMvc.perform(get("/faqs")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(ParticipantRole.MODERATOR.name())))
                .andExpect(status().isOk());
    }

    @Test
    void allowsFaqManagementToIdentityAdmins() throws Exception {
        mockMvc.perform(get("/faqs")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void leavesTheWebSocketHandshakeOpenSoStompCanAuthenticateOnConnect() throws Exception {
        int status = mockMvc.perform(get("/ws")).andReturn().getResponse().getStatus();

        assertThat(status).isNotIn(401, 403);
    }

    @Test
    void rejectsTheWebSocketHandshakeFromADisallowedOrigin() throws Exception {
        mockMvc.perform(get("/ws").header(HttpHeaders.ORIGIN, "https://evil.example.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokensSignedWithAnotherSecretAreRejected() throws Exception {
        String foreign = JwtTestTokens.signedWith(
                "another-secret-key-long-enough-for-hs256-signing-0123456789",
                userId, "mallory", Set.of(), Duration.ofMinutes(5));

        mockMvc.perform(get("/chats/{id}", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, JwtTestTokens.bearer(foreign)))
                .andExpect(status().isUnauthorized());
    }
}
