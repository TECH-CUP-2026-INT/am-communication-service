package co.edu.escuelaing.techcup.communications.service.command;

import java.util.Set;

public record CreateFaqCommand(Set<String> keywords, String answer) {
}
