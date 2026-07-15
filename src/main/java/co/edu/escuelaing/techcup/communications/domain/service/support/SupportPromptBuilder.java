package co.edu.escuelaing.techcup.communications.domain.service.support;

import co.edu.escuelaing.techcup.communications.domain.model.Message;

import java.util.List;


public final class SupportPromptBuilder {

    private String subject;
    private List<Message> history = List.of();

    public SupportPromptBuilder withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public SupportPromptBuilder withHistory(List<Message> history) {
        this.history = history;
        return this;
    }

    public String build() {
        StringBuilder prompt = new StringBuilder("Ticket subject: ").append(subject);
        if (!history.isEmpty()) {
            prompt.append("\n\nConversation so far:");
            for (Message message : history) {
                String role = SupportBotIdentity.BOT_USER_ID.equals(message.getSenderId()) ? "Assistant" : "User";
                prompt.append("\n- ").append(role).append(": ").append(message.getContent());
            }
        }
        return prompt.toString();
    }
}
