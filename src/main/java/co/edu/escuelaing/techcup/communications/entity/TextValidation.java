package co.edu.escuelaing.techcup.communications.entity;

import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;

/**
 * Shared guard against control characters in free-text fields (message content, ticket
 * subjects, report reasons). The text is stored and re-broadcast verbatim - escaping for
 * display is the client's job - but control characters serve no legitimate purpose here and
 * enable log injection or terminal/log-viewer trickery, so they are rejected outright.
 */
final class TextValidation {

    private TextValidation() {
    }

    static void rejectControlCharacters(String value, String fieldName) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\n' && c != '\t' && Character.isISOControl(c)) {
                throw new InvalidChatOperationException(fieldName + " must not contain control characters");
            }
        }
    }
}
