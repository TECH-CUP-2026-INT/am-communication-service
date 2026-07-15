package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Pure O/R mapping for the {@code faqs} table. Holds no business logic or validation. */
@Entity
@Table(name = "faqs")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class FaqDao {

    public static final int MAX_ANSWER_LENGTH = Faq.MAX_ANSWER_LENGTH;
    public static final int MAX_KEYWORD_LENGTH = Faq.MAX_KEYWORD_LENGTH;

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
}
