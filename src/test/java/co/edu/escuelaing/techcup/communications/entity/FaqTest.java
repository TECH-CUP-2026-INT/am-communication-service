package co.edu.escuelaing.techcup.communications.entity;

import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FaqTest {

    @Test
    void createsFaqWithNormalizedKeywords() {
        Faq faq = Faq.create(Set.of("Password", " login "), "Reset it from the login screen.");

        assertThat(faq.getId()).isNotNull();
        assertThat(faq.getKeywords()).containsExactlyInAnyOrder("password", "login");
        assertThat(faq.getAnswer()).isEqualTo("Reset it from the login screen.");
        assertThat(faq.getCreatedAt()).isNotNull();
        assertThat(faq.getUpdatedAt()).isEqualTo(faq.getCreatedAt());
    }

    @Test
    void rejectsBlankAnswer() {
        assertThatThrownBy(() -> Faq.create(Set.of("password"), " "))
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void rejectsAnswerOverMaxLength() {
        String tooLong = "a".repeat(Faq.MAX_ANSWER_LENGTH + 1);
        assertThatThrownBy(() -> Faq.create(Set.of("password"), tooLong))
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void rejectsEmptyKeywords() {
        assertThatThrownBy(() -> Faq.create(Set.of(), "answer"))
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void rejectsTooManyKeywords() {
        Set<String> keywords = new java.util.HashSet<>();
        for (int i = 0; i < Faq.MAX_KEYWORDS + 1; i++) {
            keywords.add("keyword" + i);
        }
        assertThatThrownBy(() -> Faq.create(keywords, "answer"))
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void rejectsBlankKeyword() {
        assertThatThrownBy(() -> Faq.create(Set.of("   "), "answer"))
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void rejectsKeywordOverMaxLength() {
        String tooLong = "a".repeat(Faq.MAX_KEYWORD_LENGTH + 1);
        assertThatThrownBy(() -> Faq.create(Set.of(tooLong), "answer"))
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void matchesCaseInsensitiveSubstring() {
        Faq faq = Faq.create(Set.of("password"), "Reset it from the login screen.");

        assertThat(faq.matches("I forgot my PASSWORD")).isTrue();
        assertThat(faq.matches("billing question")).isFalse();
    }

    @Test
    void updateReplacesFieldsAndBumpsUpdatedAt() throws InterruptedException {
        Faq faq = Faq.create(Set.of("password"), "old answer");
        Instant originalUpdatedAt = faq.getUpdatedAt();
        Thread.sleep(5);

        faq.update(Set.of("billing"), "new answer");

        assertThat(faq.getKeywords()).containsExactly("billing");
        assertThat(faq.getAnswer()).isEqualTo("new answer");
        assertThat(faq.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void updateRevalidatesFields() {
        Faq faq = Faq.create(Set.of("password"), "answer");

        assertThatThrownBy(() -> faq.update(Set.of("password"), " "))
                .isInstanceOf(InvalidChatOperationException.class);
    }
}
