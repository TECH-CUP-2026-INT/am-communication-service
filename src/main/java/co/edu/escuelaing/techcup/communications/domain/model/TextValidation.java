package co.edu.escuelaing.techcup.communications.domain.model;

import co.edu.escuelaing.techcup.communications.domain.exception.InvalidChatOperationException;

final class TextValidation {

    private TextValidation() {
    }

    static void rejectControlCharacters(String value, String fieldName) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\n' && c != '\t' && Character.isISOControl(c)) {
                throw new InvalidChatOperationException(fieldName + " no puede contener caracteres de control");
            }
        }
    }
}
