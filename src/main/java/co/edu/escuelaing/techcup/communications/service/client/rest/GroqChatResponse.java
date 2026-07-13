package co.edu.escuelaing.techcup.communications.service.client.rest;

import java.util.List;

record GroqChatResponse(List<GroqChatChoice> choices) {
}
