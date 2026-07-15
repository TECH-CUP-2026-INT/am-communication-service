package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "groq-chatbot",
        url = "${integrations.groq.base-url}",
        configuration = GroqFeignClientConfig.class)
interface GroqChatFeignClient {

    @PostMapping("/chat/completions")
    GroqChatResponse createChatCompletion(@RequestBody GroqChatRequest request);
}
