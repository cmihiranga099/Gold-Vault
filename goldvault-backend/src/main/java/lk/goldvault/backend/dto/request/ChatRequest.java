package lk.goldvault.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatRequest {

    /** The visitor's new message. */
    private String message;

    /** Prior turns in the conversation, oldest first. Capped client-side to the last ~10. */
    private List<ChatTurn> history;

    /** Optional context the frontend already knows from the logged-in session — never trusted
     *  for anything sensitive on its own; the backend re-validates ownership before returning
     *  any customer- or shop-specific data. */
    private Long customerId;
    private Long shopId;

    /** "PUBLIC" | "CUSTOMER" | "SHOP" | "ADMIN" — which portal the widget is mounted in. */
    private String portal;

    @Getter
    @Setter
    public static class ChatTurn {
        private String role;   // "user" | "assistant"
        private String content;
    }
}