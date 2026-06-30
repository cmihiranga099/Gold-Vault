package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.PlaceBidRequest;
import lk.goldvault.backend.dto.response.AuctionBidResponse;
import lk.goldvault.backend.dto.response.AuctionResponse;
import lk.goldvault.backend.dto.response.GoldItemResponse;
import lk.goldvault.backend.entity.*;
import lk.goldvault.backend.enums.TicketStatus;
import lk.goldvault.backend.repository.AuctionBidRepository;
import lk.goldvault.backend.repository.AuctionRepository;
import lk.goldvault.backend.repository.PawnTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService {

    private final AuctionRepository    auctionRepository;
    private final AuctionBidRepository auctionBidRepository;
    private final PawnTicketRepository pawnTicketRepository;

    private static final int GRACE_PERIOD_DAYS = 14;  // days after expiry before auction starts
    private static final int AUCTION_DURATION_DAYS = 14; // how long bidding stays open

    // ── Scheduled job: create auctions for tickets past grace period ──────────────

    @Transactional
    public int createAuctionsForOverdueTickets() {
        LocalDate cutoff = LocalDate.now().minusDays(GRACE_PERIOD_DAYS);

        List<PawnTicket> overdue = pawnTicketRepository
                .findByStatusAndExpiryDateBefore(TicketStatus.EXPIRED, cutoff);

        int created = 0;
        for (PawnTicket ticket : overdue) {
            if (auctionRepository.existsByTicketId(ticket.getId())) continue;

            Auction auction = Auction.builder()
                    .ticket(ticket)
                    .shop(ticket.getShop())
                    .startingPrice(ticket.getLoanAmount())
                    .currentBid(null)
                    .status("OPEN")
                    .startsAt(LocalDateTime.now())
                    .endsAt(LocalDateTime.now().plusDays(AUCTION_DURATION_DAYS))
                    .build();

            auctionRepository.save(auction);

            ticket.setStatus(TicketStatus.AUCTIONED);
            pawnTicketRepository.save(ticket);

            created++;
            log.info("[Auction] Created auction for ticket {} (loan: {})",
                    ticket.getTicketNumber(), ticket.getLoanAmount());
        }
        return created;
    }

    // ── Manual creation (admin triggers immediately, skipping grace period) ───────

    @Transactional
    public AuctionResponse createAuctionManually(Long ticketId) {
        PawnTicket ticket = pawnTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        if (ticket.getStatus() != TicketStatus.EXPIRED) {
            throw new RuntimeException(
                    "Only EXPIRED tickets can be sent to auction. Current status: " + ticket.getStatus());
        }
        if (auctionRepository.existsByTicketId(ticketId)) {
            throw new RuntimeException("This ticket already has an auction.");
        }

        Auction auction = Auction.builder()
                .ticket(ticket)
                .shop(ticket.getShop())
                .startingPrice(ticket.getLoanAmount())
                .status("OPEN")
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusDays(AUCTION_DURATION_DAYS))
                .build();

        auction = auctionRepository.save(auction);

        ticket.setStatus(TicketStatus.AUCTIONED);
        pawnTicketRepository.save(ticket);

        return toResponse(auction);
    }

    // ── Bidding ──────────────────────────────────────────────────────────────────

    @Transactional
    public AuctionResponse placeBid(Long auctionId, PlaceBidRequest request) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found: " + auctionId));

        if (!"OPEN".equals(auction.getStatus())) {
            throw new RuntimeException("This auction is no longer open for bidding.");
        }
        if (LocalDateTime.now().isAfter(auction.getEndsAt())) {
            throw new RuntimeException("This auction has ended.");
        }

        var minimumBid = auction.getCurrentBid() != null
                ? auction.getCurrentBid()
                : auction.getStartingPrice();

        if (request.getAmount().compareTo(minimumBid) <= 0) {
            throw new RuntimeException(
                    String.format("Your bid must be higher than the current bid of LKR %.2f", minimumBid));
        }

        AuctionBid bid = AuctionBid.builder()
                .auction(auction)
                .bidderName(request.getBidderName())
                .bidderPhone(request.getBidderPhone())
                .amount(request.getAmount())
                .build();

        bid = auctionBidRepository.save(bid);

        auction.setCurrentBid(request.getAmount());
        auction = auctionRepository.save(auction);

        return toResponse(auction);
    }

    // ── Close / finalize an auction ────────────────────────────────────────────────

    @Transactional
    public AuctionResponse closeAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found: " + auctionId));

        if (!"OPEN".equals(auction.getStatus())) {
            throw new RuntimeException("Auction is already " + auction.getStatus());
        }

        List<AuctionBid> bids = auctionBidRepository.findByAuctionIdOrderByAmountDesc(auctionId);

        if (!bids.isEmpty()) {
            AuctionBid winner = bids.get(0);
            auction.setWinningBid(winner);
        }

        auction.setStatus("CLOSED");
        auction = auctionRepository.save(auction);

        return toResponse(auction);
    }

    @Transactional
    public AuctionResponse cancelAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found: " + auctionId));

        auction.setStatus("CANCELLED");
        auction = auctionRepository.save(auction);
        return toResponse(auction);
    }

    /** Scheduled job: auto-close any OPEN auctions whose end time has passed. */
    @Transactional
    public int autoCloseExpiredAuctions() {
        List<Auction> toClose = auctionRepository
                .findByStatusAndEndsAtBefore("OPEN", LocalDateTime.now());

        int closed = 0;
        for (Auction auction : toClose) {
            List<AuctionBid> bids = auctionBidRepository.findByAuctionIdOrderByAmountDesc(auction.getId());
            if (!bids.isEmpty()) {
                auction.setWinningBid(bids.get(0));
            }
            auction.setStatus("CLOSED");
            auctionRepository.save(auction);
            closed++;
        }
        return closed;
    }

    // ── Read ─────────────────────────────────────────────────────────────────────

    public List<AuctionResponse> getOpenAuctions() {
        return auctionRepository.findByStatusOrderByEndsAtAsc("OPEN")
                .stream().map(this::toResponse).toList();
    }

    public AuctionResponse getById(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found: " + id));
        return toResponse(auction);
    }

    public List<AuctionResponse> getByShop(Long shopId) {
        return auctionRepository.findByShopIdOrderByCreatedAtDesc(shopId)
                .stream().map(this::toResponse).toList();
    }

    public List<AuctionBidResponse> getBidHistory(Long auctionId) {
        return auctionBidRepository.findByAuctionIdOrderByAmountDesc(auctionId)
                .stream()
                .map(b -> AuctionBidResponse.builder()
                        .id(b.getId())
                        .bidderName(maskName(b.getBidderName()))
                        .amount(b.getAmount())
                        .createdAt(b.getCreatedAt())
                        .build())
                .toList();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private String maskName(String name) {
        if (name == null || name.length() <= 2) return name;
        return name.charAt(0) + "***" + name.charAt(name.length() - 1);
    }

    private AuctionResponse toResponse(Auction auction) {
        PawnTicket ticket = auction.getTicket();

        List<GoldItemResponse> items = ticket.getGoldItems() == null ? List.of() :
                ticket.getGoldItems().stream()
                        .map(item -> GoldItemResponse.builder()
                                .id(item.getId())
                                .description(item.getDescription())
                                .goldType(item.getGoldType())
                                .weightGrams(item.getWeightGrams())
                                .purity(item.getPurity())
                                .estimatedValue(item.getEstimatedValue())
                                .photoUrl(item.getPhotoUrl())
                                .build())
                        .toList();

        int bidCount = auction.getBids() != null ? auction.getBids().size() : 0;

        return AuctionResponse.builder()
                .id(auction.getId())
                .ticketId(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .shopId(auction.getShop().getId())
                .shopName(auction.getShop().getName())
                .startingPrice(auction.getStartingPrice())
                .currentBid(auction.getCurrentBid())
                .bidCount(bidCount)
                .status(auction.getStatus())
                .startsAt(auction.getStartsAt())
                .endsAt(auction.getEndsAt())
                .ended(LocalDateTime.now().isAfter(auction.getEndsAt()))
                .goldItems(items)
                .winningBidderName(auction.getWinningBid() != null
                        ? maskName(auction.getWinningBid().getBidderName()) : null)
                .build();
    }
}