package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.GoldListingRequest;
import lk.goldvault.backend.dto.response.GoldListingResponse;
import lk.goldvault.backend.dto.response.GoldOfferResponse;
import lk.goldvault.backend.entity.Customer;
import lk.goldvault.backend.entity.GoldListing;
import lk.goldvault.backend.entity.GoldOffer;
import lk.goldvault.backend.enums.ListingStatus;
import lk.goldvault.backend.repository.CustomerRepository;
import lk.goldvault.backend.repository.GoldListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoldListingService {

    private final GoldListingRepository goldListingRepository;
    private final CustomerRepository customerRepository;

    public GoldListingResponse createListing(Long customerId, GoldListingRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        GoldListing listing = GoldListing.builder()
                .customer(customer)
                .description(request.getDescription())
                .weightGrams(request.getWeightGrams())
                .purity(request.getPurity())
                .askingPrice(request.getAskingPrice())
                .status(ListingStatus.OPEN)
                .build();

        listing = goldListingRepository.save(listing);
        return toResponse(listing);
    }

    public GoldListingResponse getById(Long id) {
        GoldListing listing = goldListingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found with id: " + id));
        return toResponse(listing);
    }

    public List<GoldListingResponse> getByCustomer(Long customerId) {
        return goldListingRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Public marketplace view — all open listings, visible to shops browsing for gold to buy. */
    public List<GoldListingResponse> getOpenListings() {
        return goldListingRepository.findByStatus(ListingStatus.OPEN)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GoldListingResponse withdraw(Long id, Long customerId) {
        GoldListing listing = goldListingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found with id: " + id));

        if (!listing.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("This listing does not belong to the given customer");
        }
        if (listing.getStatus() == ListingStatus.SOLD) {
            throw new RuntimeException("Cannot withdraw a listing that has already been sold");
        }

        listing.setStatus(ListingStatus.WITHDRAWN);
        listing = goldListingRepository.save(listing);
        return toResponse(listing);
    }

    GoldListingResponse toResponse(GoldListing listing) {
        List<GoldOfferResponse> offerResponses = listing.getOffers() == null ? List.of() :
                listing.getOffers().stream()
                        .map(this::toOfferResponse)
                        .toList();

        BigDecimal bestOffer = offerResponses.stream()
                .map(GoldOfferResponse::getOfferPrice)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return GoldListingResponse.builder()
                .id(listing.getId())
                .customerId(listing.getCustomer().getId())
                .customerName(listing.getCustomer().getFullName())
                .description(listing.getDescription())
                .weightGrams(listing.getWeightGrams())
                .purity(listing.getPurity())
                .askingPrice(listing.getAskingPrice())
                .status(listing.getStatus())
                .createdAt(listing.getCreatedAt())
                .offers(offerResponses)
                .bestOfferPrice(bestOffer)
                .build();
    }

    private GoldOfferResponse toOfferResponse(GoldOffer offer) {
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