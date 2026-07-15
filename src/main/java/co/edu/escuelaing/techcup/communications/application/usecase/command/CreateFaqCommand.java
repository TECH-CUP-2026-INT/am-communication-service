package co.edu.escuelaing.techcup.communications.application.usecase.command;

import java.util.Set;

public record CreateFaqCommand(Set<String> keywords, String answer) {
}
