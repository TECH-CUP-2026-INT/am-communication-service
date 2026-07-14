package co.edu.escuelaing.techcup.communications.service.client.feign;

import java.util.List;

record GroqChatResponse(List<GroqChatChoice> choices) {
}
