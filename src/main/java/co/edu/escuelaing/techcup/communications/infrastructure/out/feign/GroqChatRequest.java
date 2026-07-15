package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import java.util.List;

record GroqChatRequest(String model, List<GroqChatMessage> messages) {
}
