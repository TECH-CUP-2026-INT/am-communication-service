package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import java.util.List;

record GroqChatResponse(List<GroqChatChoice> choices) {
}
