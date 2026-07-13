package co.edu.escuelaing.techcup.communications.repository;

import co.edu.escuelaing.techcup.communications.entity.Faq;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FaqRepositoryTest {

    @Autowired
    private FaqRepository faqRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void roundTripsKeywordsThroughTheElementCollection() {
        Faq faq = Faq.create(Set.of("password", "login"), "Reset it from the login screen.");
        faqRepository.save(faq);
        em.flush();
        em.clear();

        Faq reloaded = faqRepository.findById(faq.getId()).orElseThrow();
        assertThat(reloaded.getKeywords()).containsExactlyInAnyOrder("password", "login");
        assertThat(reloaded.getAnswer()).isEqualTo("Reset it from the login screen.");
    }

    @Test
    void findsAllOrderedByCreatedAtAscending() throws InterruptedException {
        Faq first = faqRepository.save(Faq.create(Set.of("password"), "first"));
        Thread.sleep(5);
        Faq second = faqRepository.save(Faq.create(Set.of("billing"), "second"));
        em.flush();
        em.clear();

        List<Faq> all = faqRepository.findAllByOrderByCreatedAtAsc();

        assertThat(all).extracting(Faq::getId).containsExactly(first.getId(), second.getId());
    }
}
