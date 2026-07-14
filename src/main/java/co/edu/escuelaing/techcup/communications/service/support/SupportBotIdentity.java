package co.edu.escuelaing.techcup.communications.service.support;

import java.util.UUID;

/**
 * Well-known participant id used when an automated support tier (FAQ, chatbot) posts a
 * message into a ticket's chat. Added as a participant once, at ticket creation.
 */
public final class SupportBotIdentity {

    public static final UUID BOT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private SupportBotIdentity() {
    }
}
