package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.GoldOfferRequest;
import lk.goldvault.backend.dto.response.GoldOfferResponse;
import lk.goldvault.backend.entity.GoldListing;
import lk.goldvault.backend.entity.GoldOffer;
import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.enums.ListingStatus;
import lk.goldvault.backend.enums.OfferStatus;
import lk.goldvault.backend.repository.GoldListingRepository;
import lk.goldvault.backend.repository.GoldOfferRepository;
import lk.goldvault.backend.repository.PawnShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoldOfferService {

    private final GoldOfferRepository goldOfferRepository;
    private final GoldListingRepository goldListingRepository;
    private final PawnShopRepository pawnShopRepository;

    public GoldOfferResponse submitOffer(Long shopId, GoldOfferRequest request) {
        GoldListing listing = goldListingRepository.findById(request.getListingId())
                .orElseThrow(() -> new RuntimeException(
                        "Listing not found with id: " + request.getListingId()));

        if (listing.getStatus() != ListingStatus.OPEN) {
            throw new RuntimeException(
                    "Cannot submit an offer on a listing with status: " + listing.getStatus());
        }

        PawnShop shop = pawnShopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));

        GoldOffer offer = GoldOffer.builder()
                .listing(listing)
                .shop(shop)
                .offerPrice(request.getOfferPrice())
                .message(request.getMessage())
                .status(OfferStatus.PENDING)
                .build();

        offer = goldOfferRepository.save(offer);

        // Mark the listing as under review now that it has at least one offer
        listing.setStatus(ListingStatus.UNDER_REVIEW);
        goldListingRepository.save(listing);

        return toResponse(offer);
    }

    public List<GoldOfferResponse> getByListing(Long listingId) {
        return goldOfferRepository.findByListingId(listingId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<GoldOfferResponse> getByShop(Long shopId) {
        return goldOfferRepository.findByShopId(shopId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Customer accepts an offer. The accepted offer's listing becomes SOLD,
     * all other PENDING offers on the same listing become REJECTED.
     */
    @Transactional
    public GoldOfferResponse acceptOffer(Long offerId, Long customerId) {
        GoldOffer offer = goldOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with id: " + offerId));

        GoldListing listing = offer.getListing();

        if (!listing.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("This offer is not on a listing belonging to the given customer");
        }
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new RuntimeException("Only PENDING offers can be accepted. Current status: " + offer.getStatus());
        }

        offer.setStatus(OfferStatus.ACCEPTED);
        goldOfferRepository.save(offer);

        listing.setStatus(ListingStatus.SOLD);
        goldListingRepository.save(listing);

        // Reject all other pending offers on the same listing
        List<GoldOffer> otherOffers = goldOfferRepository.findByListingId(listing.getId());
        for (GoldOffer other : otherOffers) {
            if (!other.getId().equals(offer.getId()) && other.getStatus() == OfferStatus.PENDING) {
                other.setStatus(OfferStatus.REJECTED);
                goldOfferRepository.save(other);
            }
        }

        return toResponse(offer);
    }

    public GoldOfferResponse rejectOffer(Long offerId, Long customerId) {
        GoldOffer offer = goldOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with id: " + offerId));

        if (!offer.getListing().getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("This offer is not on a listing belonging to the given customer");
        }
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new RuntimeException("Only PENDING offers can be rejected. Current status: " + offer.getStatus());
        }

        offer.setStatus(OfferStatus.REJECTED);
        offer = goldOfferRepository.save(offer);

        // If no PENDING offers remain on the listing, reopen it
        boolean anyPending = goldOfferRepository.findByListingId(offer.getListing().getId())
                .stream()
                .anyMatch(o -> o.getStatus() == OfferStatus.PENDING);
        if (!anyPending) {
            GoldListing listing = offer.getListing();
            listing.setStatus(ListingStatus.OPEN);
            goldListingRepository.save(listing);
        }

        return toResponse(offer);
    }

    public GoldOfferResponse withdrawOffer(Long offerId, Long shopId) {
        GoldOffer offer = goldOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with id: " + offerId));

        if (!offer.getShop().getId().equals(shopId)) {
            throw new RuntimeException("This offer does not belong to the given shop");
        }
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new RuntimeException("Only PENDING offers can be withdrawn. Current status: " + offer.getStatus());
        }

        offer.setStatus(OfferStatus.WITHDRAWN);
        return toResponse(goldOfferRepository.save(offer));
    }

    private GoldOfferResponse toResponse(GoldOffer offer) {
        return GoldOfferResponse.builder()
                .id(offer.getId())
                .listingId(offer.getListing().getId())
                .shopId(offer.getShop().getId())
                .shopName(offer.getShop().getName())
                .offerPrice(offer.getOfferPrice())
                .message(offer.getMessage())
                .status(offer.getStatus())
                .createdAt(offer.getCreatedAt())
                .build();
    }
}