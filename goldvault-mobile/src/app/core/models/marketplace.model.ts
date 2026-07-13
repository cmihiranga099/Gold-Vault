export type GoldPurity = 'K24' | 'K22' | 'K21' | 'K18' | 'P916' | 'P750' | 'OTHER';
export type ListingStatus = 'OPEN' | 'UNDER_REVIEW' | 'SOLD' | 'WITHDRAWN';
export type OfferStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'WITHDRAWN';

export interface GoldRateRequest {
  purity: GoldPurity;
  ratePerGram: number;
}

export interface GoldRateResponse {
  id: number;
  shopId: number;
  shopName: string;
  purity: GoldPurity;
  ratePerGram: number;
  effectiveDate: string;
  active: boolean;
}

export interface GoldListingRequest {
  description?: string;
  weightGrams: number;
  purity: GoldPurity;
  askingPrice?: number;
}

export interface GoldOfferResponse {
  id: number;
  listingId: number;
  shopId: number;
  shopName: string;
  offerPrice: number;
  message: string | null;
  status: OfferStatus;
  createdAt: string;
}

export interface GoldListingResponse {
  id: number;
  customerId: number;
  customerName: string;
  description: string | null;
  weightGrams: number;
  purity: GoldPurity;
  askingPrice: number | null;
  status: ListingStatus;
  createdAt: string;
  offers: GoldOfferResponse[];
  bestOfferPrice: number | null;
}

export interface GoldOfferRequest {
  listingId: number;
  offerPrice: number;
  message?: string;
}