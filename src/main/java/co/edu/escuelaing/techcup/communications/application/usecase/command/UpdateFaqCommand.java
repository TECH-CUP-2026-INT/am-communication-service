package co.edu.escuelaing.techcup.communications.application.usecase.command;

import java.util.Set;
import java.util.UUID;

public record UpdateFaqCommand(UUID faqId, Set<String> keywords, String answer) {
}
