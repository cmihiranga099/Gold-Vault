package lk.goldvault.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.goldvault.backend.dto.request.ChatRequest;
import lk.goldvault.backend.dto.response.ChatResponse;
import lk.goldvault.backend.dto.response.MarketRateResponse;
import lk.goldvault.backend.dto.response.PawnTicketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hybrid support chatbot for GoldVault.
 *
 * Order of operations for every incoming message:
 *   1. Try each rule-based intent matcher (instant, free, no external call, and able to
 *      pull real live data — gold rates, a specific ticket's status, etc).
 *   2. If nothing matches confidently, fall through to the configured LLM (Anthropic Claude)
 *      for an open-ended answer, grounded by a system prompt describing GoldVault.
 *   3. If no LLM API key is configured, degrade gracefully to a "contact support" message
 *      instead of erroring.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final MarketGoldRateService marketGoldRateService;
    private final PawnTicketService     pawnTicketService;
    private final ObjectMapper          objectMapper = new ObjectMapper();

    @Value("${anthropic.api.key:}")
    private String anthropicApiKey;

    @Value("${anthropic.api.model:claude-haiku-4-5-20251001}")
    private String anthropicModel;

    private static final Pattern TICKET_NUMBER_PATTERN =
            Pattern.compile("GV-\\d{4}-\\d{6}", Pattern.CASE_INSENSITIVE);

    private static final Pattern WEIGHT_PATTERN =
            Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(g|gram|grams)", Pattern.CASE_INSENSITIVE);

    private static final Pattern PURITY_PATTERN =
            Pattern.compile("(24k|22k|18k|916|750)", Pattern.CASE_INSENSITIVE);

    public ChatResponse respond(ChatRequest request) {
        String message = request.getMessage() == null ? "" : request.getMessage().trim();
        String lower   = message.toLowerCase(Locale.ROOT);

        if (message.isBlank()) {
            return rule("Could you tell me a bit more about what you need help with?");
        }

        // ── 1. Specific ticket lookup ────────────────────────────────────────────
        Matcher ticketMatcher = TICKET_NUMBER_PATTERN.matcher(message);
        if (ticketMatcher.find()) {
            return ticketLookup(ticketMatcher.group(), request);
        }

        // ── 2. Live gold rates ───────────────────────────────────────────────────
        if (containsAny(lower, "gold rate", "buying rate", "today's rate", "rate today", "gold price")) {
            return goldRatesReply();
        }

        // ── 3. Quick loan estimate ("how much for 20g of 22k gold") ─────────────
        Matcher weightMatcher = WEIGHT_PATTERN.matcher(lower);
        Matcher purityMatcher = PURITY_PATTERN.matcher(lower);
        if ((containsAny(lower, "how much", "loan for", "borrow", "worth") ) && weightMatcher.find()) {
            return quickEstimate(weightMatcher, purityMatcher);
        }

        // ── 4. Navigation intents ───────────────────────────────────────────────
        if (containsAny(lower, "find shop", "shops near", "shop near", "nearest shop")) {
            return rule(
                "You can search for pawn shops near you, compare their gold rates, and see reviews.",
                link("Find shops near you", "/find-shops")
            );
        }
        if (containsAny(lower, "auction", "bidding", "unredeemed")) {
            return rule(
                "Unredeemed gold from expired tickets goes to auction. You can browse open auctions and place bids.",
                link("View live auctions", "/auctions")
            );
        }
        if (containsAny(lower, "calculator", "how much interest", "monthly payment")) {
            return rule(
                "Our loan calculator lets you estimate repayments before visiting a shop — just enter the weight, purity, and loan amount.",
                link("Open loan calculator", "/calculator")
            );
        }
        if (containsAny(lower, "register", "sign up", "create account", "become a customer")) {
            return rule(
                "You can register as a customer for free, or if you run a pawn shop, register your shop for approval.",
                link("Customer registration", "/auth/register"),
                link("Shop registration", "/auth/shop-register")
            );
        }

        // ── 5. Canned FAQ ────────────────────────────────────────────────────────
        String faq = matchFaq(lower);
        if (faq != null) {
            return rule(faq);
        }

        // ── 6. Greeting ──────────────────────────────────────────────────────────
        if (containsAny(lower, "hi", "hello", "hey", "good morning", "good afternoon")) {
            return rule("Hi! I'm the GoldVault assistant. I can check gold rates, look up a ticket by number, " +
                    "estimate a loan, or answer questions about pawning, auctions, and how GoldVault works. What do you need?");
        }

        // ── 7. Fall through to the LLM for anything open-ended ──────────────────
        return aiFallback(request);
    }

    // ── Rule handlers ────────────────────────────────────────────────────────────

    private ChatResponse ticketLookup(String ticketNumber, ChatRequest request) {
        PawnTicketResponse ticket;
        try {
            ticket = pawnTicketService.getByTicketNumber(ticketNumber.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return rule("I couldn't find a ticket with number " + ticketNumber.toUpperCase(Locale.ROOT) + ". Double-check the number, or contact your shop directly.");
        }

        boolean ownsAsCustomer = request.getCustomerId() != null && request.getCustomerId().equals(ticket.getCustomerId());
        boolean ownsAsShop     = request.getShopId() != null && request.getShopId().equals(ticket.getShopId());

        if (!ownsAsCustomer && !ownsAsShop) {
            return rule("I found that ticket, but I can only share its details if you're logged in as the customer or shop it belongs to. Please log in and try again.");
        }

        String reply = String.format(Locale.ROOT,
                "Ticket %s: status %s, loan amount LKR %,.2f, outstanding balance LKR %,.2f, due %s.%s",
                ticket.getTicketNumber(),
                ticket.getStatus(),
                ticket.getLoanAmount(),
                ticket.getOutstandingBalance(),
                ticket.getExpiryDate(),
                ticket.isOverdue() ? " This ticket is overdue." : ""
        );
        return rule(reply);
    }

    private ChatResponse goldRatesReply() {
        List<MarketRateResponse> rates = marketGoldRateService.getLatestMarketRates();
        if (rates.isEmpty()) {
            return rule("Rates haven't been published yet today. Check back shortly, or view individual shop rates on the gold rates page.",
                    link("View gold rates", "/gold-rates"));
        }
        StringBuilder sb = new StringBuilder("Today's market gold rates: ");
        for (MarketRateResponse r : rates) {
            sb.append(String.format(Locale.ROOT, "%s: LKR %,.2f/g. ", r.getPurity(), r.getRatePerGram()));
        }
        return rule(sb.toString().trim(), link("View full rates & trend", "/gold-rates"));
    }

    private ChatResponse quickEstimate(Matcher weightMatcher, Matcher purityMatcher) {
        BigDecimal weight = new BigDecimal(weightMatcher.group(1));
        String purityRaw  = purityMatcher.find() ? purityMatcher.group(1).toUpperCase(Locale.ROOT) : "22K";
        String purity      = normalizePurity(purityRaw);

        List<MarketRateResponse> rates = marketGoldRateService.getLatestMarketRates();
        MarketRateResponse match = rates.stream()
                .filter(r -> r.getPurity().equalsIgnoreCase(purity))
                .findFirst()
                .orElse(null);

        if (match == null) {
            return rule("I don't have a published rate for " + purity + " right now — try the full calculator for an estimate.",
                    link("Open loan calculator", "/calculator"));
        }

        BigDecimal goldValue = match.getRatePerGram().multiply(weight);
        BigDecimal maxLoan   = goldValue.multiply(BigDecimal.valueOf(0.7)).setScale(2, RoundingMode.HALF_UP);

        return rule(String.format(Locale.ROOT,
                "%sg of %s gold is worth roughly LKR %,.2f at today's rate. Most shops lend up to 70%% of that — around LKR %,.2f. Exact terms vary by shop.",
                weight.stripTrailingZeros().toPlainString(), purity, goldValue, maxLoan
        ), link("Get an exact estimate", "/calculator"));
    }

    private String normalizePurity(String raw) {
        return switch (raw) {
            case "916" -> "22K";
            case "750" -> "18K";
            default -> raw;
        };
    }

    private String matchFaq(String lower) {
        if (containsAny(lower, "what is ltv", "loan to value")) {
            return "LTV (loan-to-value) is the percentage of your gold's market value a shop is willing to lend. Most GoldVault shops lend up to 70% LTV.";
        }
        if (containsAny(lower, "flat interest", "reducing interest", "interest type")) {
            return "Flat interest is calculated on the full loan amount for the whole term. Reducing interest is calculated only on the remaining balance, so it's usually cheaper overall. Rates and type vary by shop.";
        }
        if (containsAny(lower, "redeem", "get my gold back", "pay off")) {
            return "To redeem your gold, pay off the outstanding balance (loan + accrued interest) at the shop that holds your ticket. They'll return your gold items and close the ticket.";
        }
        if (containsAny(lower, "renew", "extend", "grace period")) {
            return "Most shops allow renewing a ticket by paying at least the accrued interest before the due date, which extends the term. Grace periods vary by shop — check with them directly.";
        }
        if (containsAny(lower, "documents", "what do i need", "nic", "id required")) {
            return "To pawn gold you'll typically need a valid NIC and the gold item itself. Some shops may ask for additional verification for larger loans.";
        }
        if (containsAny(lower, "loyalty", "points", "reward")) {
            return "Customers earn loyalty points for activity like on-time payments and redemptions. Points count toward reward milestones shown on your dashboard.";
        }
        if (containsAny(lower, "how does pawning work", "how does this work", "what is goldvault")) {
            return "GoldVault connects customers with licensed pawn shops across Sri Lanka. You pawn gold for a short-term loan against its value, then redeem it later by repaying the loan plus interest. Unredeemed gold from expired tickets goes to auction.";
        }
        return null;
    }

    // ── AI fallback ──────────────────────────────────────────────────────────────

    private ChatResponse aiFallback(ChatRequest request) {
        if (anthropicApiKey == null || anthropicApiKey.isBlank()) {
            return rule("I'm not able to answer that one automatically yet. For anything else, please reach out to your shop directly or contact GoldVault support.");
        }

        try {
            List<java.util.Map<String, String>> messages = new ArrayList<>();
            if (request.getHistory() != null) {
                for (ChatRequest.ChatTurn turn : request.getHistory()) {
                    messages.add(java.util.Map.of("role", turn.getRole(), "content", turn.getContent()));
                }
            }
            messages.add(java.util.Map.of("role", "user", "content", request.getMessage()));

            java.util.Map<String, Object> body = java.util.Map.of(
                    "model", anthropicModel,
                    "max_tokens", 400,
                    "system", SYSTEM_PROMPT,
                    "messages", messages
            );

            String rawResponse = RestClient.create().post()
                    .uri("https://api.anthropic.com/v1/messages")
                    .header("x-api-key", anthropicApiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(rawResponse);
            String text = root.path("content").get(0).path("text").asText();
            return ChatResponse.builder().reply(text).source("AI").build();

        } catch (Exception e) {
            log.error("Chatbot AI fallback failed", e);
            return rule("I couldn't reach the AI assistant just now. Please try rephrasing, or contact support directly.");
        }
    }

    private static final String SYSTEM_PROMPT = """
            You are the GoldVault support assistant, embedded on a Sri Lankan digital pawn shop
            and gold marketplace platform. Be concise, friendly, and factual. You can discuss how
            pawning, interest, redemption, auctions, and the marketplace work in general terms.
            Never invent specific numbers for a person's account, ticket, or shop — if asked for
            account-specific data you don't have, tell them to check their dashboard or log in.
            Do not give legal or financial advice beyond general education. Keep replies under
            120 words unless the question genuinely needs more detail.
            """;

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private boolean containsAny(String haystack, String... needles) {
        for (String n : needles) {
            if (haystack.contains(n)) return true;
        }
        return false;
    }

    private ChatResponse rule(String reply, ChatResponse.SuggestedLink... links) {
        return ChatResponse.builder()
                .reply(reply)
                .source("RULE")
                .links(links.length == 0 ? null : List.of(links))
                .build();
    }

    private ChatResponse.SuggestedLink link(String label, String url) {
        return ChatResponse.SuggestedLink.builder().label(label).url(url).build();
    }
}