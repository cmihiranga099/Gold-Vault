package lk.goldvault.backend.controller;

import lk.goldvault.backend.dto.request.ChatRequest;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.ChatResponse;
import lk.goldvault.backend.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoint — deliberately permitAll in SecurityConfig so anonymous visitors on the
 * public site can use the widget too. Any account-specific lookups are re-validated inside
 * ChatbotService against the customerId/shopId the frontend supplies, never trusted blindly.
 */
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/message")
    public ApiResponse<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        ChatResponse response = chatbotService.respond(request);
        return ApiResponse.success("OK", response);
    }
}