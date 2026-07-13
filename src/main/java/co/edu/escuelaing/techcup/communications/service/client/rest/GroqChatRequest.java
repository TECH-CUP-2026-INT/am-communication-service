package co.edu.escuelaing.techcup.communications.service.client.rest;

import java.util.List;

record GroqChatRequest(String model, List<GroqChatMessage> messages) {
}
