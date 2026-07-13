package co.edu.escuelaing.techcup.communications.entity;

import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
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

@Entity
@Table(name = "faqs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Faq {

    public static final int MAX_ANSWER_LENGTH = 2000;
    public static final int MAX_KEYWORD_LENGTH = 100;
    public static final int MAX_KEYWORDS = 20;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "faq_keywords", joinColumns = @JoinColumn(name = "faq_id"))
    @Column(name = "keyword", nullable = false, length = MAX_KEYWORD_LENGTH)
    private Set<String> keywords = new LinkedHashSet<>();

    @Column(nullable = false, length = MAX_ANSWER_LENGTH)
    private String answer;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
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

    public void update(Set<String> keywords, String answer) {
        this.keywords = validateKeywords(keywords);
        this.answer = validateAnswer(answer);
        touch();
    }

    /** Case-insensitive substring match of any keyword against the given subject. */
    public boolean matches(String subject) {
        String normalized = subject.toLowerCase(Locale.ROOT);
        return keywords.stream().anyMatch(normalized::contains);
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static Set<String> validateKeywords(Set<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            throw new InvalidChatOperationException("FAQ must have at least one keyword");
        }
        if (keywords.size() > MAX_KEYWORDS) {
            throw new InvalidChatOperationException("FAQ cannot have more than " + MAX_KEYWORDS + " keywords");
        }
        Set<String> normalized = keywords.stream()
                .map(keyword -> validateKeyword(keyword).toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalized.isEmpty()) {
            throw new InvalidChatOperationException("FAQ must have at least one keyword");
        }
        return normalized;
    }

    private static String validateKeyword(String keyword) {
        Objects.requireNonNull(keyword, "keyword must not be null");
        String trimmed = keyword.trim();
        if (trimmed.isBlank()) {
            throw new InvalidChatOperationException("FAQ keyword must not be blank");
        }
        if (trimmed.length() > MAX_KEYWORD_LENGTH) {
            throw new InvalidChatOperationException("Keyword exceeds " + MAX_KEYWORD_LENGTH + " characters");
        }
        TextValidation.rejectControlCharacters(trimmed, "FAQ keyword");
        return trimmed;
    }

    private static String validateAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            throw new InvalidChatOperationException("FAQ answer must not be blank");
        }
        if (answer.length() > MAX_ANSWER_LENGTH) {
            throw new InvalidChatOperationException("Answer exceeds " + MAX_ANSWER_LENGTH + " characters");
        }
        TextValidation.rejectControlCharacters(answer, "FAQ answer");
        return answer;
    }
}
