package co.edu.escuelaing.techcup.communications.domain.model;

import co.edu.escuelaing.techcup.communications.domain.exception.InvalidChatOperationException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Faq {

    public static final int MAX_ANSWER_LENGTH = 2000;
    public static final int MAX_KEYWORD_LENGTH = 100;
    public static final int MAX_KEYWORDS = 20;

    private UUID id;
    private Set<String> keywords = new LinkedHashSet<>();
    private String answer;
    private Instant createdAt;
    private Instant updatedAt;

    private Faq(Set<String> keywords, String answer) {
        this.id = UUID.randomUUID();
        this.keywords = validateKeywords(keywords);
        this.answer = validateAnswer(answer);
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static Faq create(Set<String> keywords, String answer) {
        return new Faq(keywords, answer);
    }

    /** Rebuilds a Faq from already-valid stored state. Persistence mappers only. */
    public static Faq fromPersistence(UUID id, Set<String> keywords, String answer, Instant createdAt, Instant updatedAt) {
        Faq faq = new Faq();
        faq.id = id;
        faq.keywords = new LinkedHashSet<>(keywords);
        faq.answer = answer;
        faq.createdAt = createdAt;
        faq.updatedAt = updatedAt;
        return faq;
    }

    public void update(Set<String> keywords, String answer) {
        this.keywords = validateKeywords(keywords);
        this.answer = validateAnswer(answer);
        touch();
    }

    public boolean matches(String subject) {
        String normalized = subject.toLowerCase(Locale.ROOT);
        return keywords.stream().anyMatch(normalized::contains);
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static Set<String> validateKeywords(Set<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            throw new InvalidChatOperationException("La FAQ debe tener al menos una palabra clave");
        }
        if (keywords.size() > MAX_KEYWORDS) {
            throw new InvalidChatOperationException("La FAQ no puede tener más de " + MAX_KEYWORDS + " palabras clave");
        }
        Set<String> normalized = keywords.stream()
                .map(keyword -> validateKeyword(keyword).toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalized.isEmpty()) {
            throw new InvalidChatOperationException("La FAQ debe tener al menos una palabra clave");
        }
        return normalized;
    }

    private static String validateKeyword(String keyword) {
        Objects.requireNonNull(keyword, "keyword must not be null");
        String trimmed = keyword.trim();
        if (trimmed.isBlank()) {
            throw new InvalidChatOperationException("La palabra clave de la FAQ no puede estar vacía");
        }
        if (trimmed.length() > MAX_KEYWORD_LENGTH) {
            throw new InvalidChatOperationException("La palabra clave supera los " + MAX_KEYWORD_LENGTH + " caracteres");
        }
        TextValidation.rejectControlCharacters(trimmed, "La palabra clave de la FAQ");
        return trimmed;
    }

    private static String validateAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            throw new InvalidChatOperationException("La respuesta de la FAQ no puede estar vacía");
        }
        if (answer.length() > MAX_ANSWER_LENGTH) {
            throw new InvalidChatOperationException("La respuesta supera los " + MAX_ANSWER_LENGTH + " caracteres");
        }
        TextValidation.rejectControlCharacters(answer, "La respuesta de la FAQ");
        return answer;
    }
}
