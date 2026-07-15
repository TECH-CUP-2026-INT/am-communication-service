package co.edu.escuelaing.techcup.communications.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param baseUrl      Groq's OpenAI-compatible API base URL.
 * @param apiKey       bearer token for the Groq API; no default on purpose — the app must fail
 *                     to start rather than run without a key.
 * @param model        chat completion model id; defaults to a current Groq-hosted Llama model.
 * @param systemPrompt context primed as the {@code system} message before every conversation,
 *                     so replies stay grounded in what this assistant actually is.
 */
@Validated
@ConfigurationProperties(prefix = "integrations.groq")
public record GroqProperties(@NotBlank String baseUrl, @NotBlank String apiKey, String model, String systemPrompt) {

    private static final String DEFAULT_SYSTEM_PROMPT = """
            You are the support assistant for Astro Merge (TechCup), a sports tournament \
            management platform. Users ask about tournaments, teams, matches, registration, \
            and account issues. Answer concisely and helpfully in the user's language. If the \
            question is unrelated to the platform, say so brieVALE Lfly instead of improvising.""";

    public GroqProperties {
        model = (model == null || model.isBlank()) ? "llama-3.3-70b-versatile" : model;
        systemPrompt = (systemPrompt == null || systemPrompt.isBlank()) ? DEFAULT_SYSTEM_PROMPT : systemPrompt;
    }
}
