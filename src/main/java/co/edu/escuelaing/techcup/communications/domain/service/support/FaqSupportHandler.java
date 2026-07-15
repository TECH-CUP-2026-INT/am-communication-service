package co.edu.escuelaing.techcup.communications.domain.service.support;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.FaqRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessageRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessagePublisher;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class FaqSupportHandler extends AbstractAutomatedSupportHandler {

    private static final String ESCALATE_HINT =
            "If this didn't fully answer your question, tap Escalate to talk to our assistant.";
    private static final String NO_MATCH_MESSAGE =
            "I couldn't find an answer to that in our frequently asked questions.";

    private final FaqRepository faqRepository;

    public FaqSupportHandler(MessageRepository messageRepository, MessagePublisher messagePublisher,
                              FaqRepository faqRepository) {
        super(messageRepository, messagePublisher);
        this.faqRepository = faqRepository;
    }

    @Override
    protected SupportLevel level() {
        return SupportLevel.FAQ;
    }

    @Override
    protected SupportResult doHandle(SupportTicket ticket) {
        String body = match(ticket.getSubject())
                .map(Faq::getAnswer)
                .orElse(NO_MATCH_MESSAGE);
        postBotMessage(ticket, body + " " + ESCALATE_HINT);
        return SupportResult.resolved(SupportLevel.FAQ);
    }

    private Optional<Faq> match(String subject) {
        return faqRepository.findAllByOrderByCreatedAtAsc().stream()
                .filter(faq -> faq.matches(subject))
                .findFirst();
    }
}
