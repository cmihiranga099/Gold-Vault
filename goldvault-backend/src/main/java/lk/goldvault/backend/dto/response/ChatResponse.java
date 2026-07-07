package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatResponse {

    private String reply;

    /** "RULE" if answered by the built-in rule engine (instant, free, no external call),
     *  "AI" if it fell through to the LLM. Useful for your own debugging/analytics —
     *  the widget doesn't need to show this to the visitor. */
    private String source;

    /** Optional quick links to show as buttons under the reply (e.g. "Open calculator"). */
    private List<SuggestedLink> links;

    @Getter
    @Builder
    public static class SuggestedLink {
        private String label;
        private String url;
    }
}