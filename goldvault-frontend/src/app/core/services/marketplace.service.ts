import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import {
  GoldListingRequest, GoldListingResponse,
  GoldOfferRequest, GoldOfferResponse,
  GoldRateRequest, GoldRateResponse
} from '../models/marketplace.model';

@Injectable({ providedIn: 'root' })
export class MarketplaceService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Public, no auth
  compareRates(): Observable<GoldRateResponse[]> {
    return this.http
      .get<ApiResponse<GoldRateResponse[]>>(`${this.apiUrl}/marketplace/rates`)
      .pipe(map((res) => res.data));
  }

  openListings(): Observable<GoldListingResponse[]> {
    return this.http
      .get<ApiResponse<GoldListingResponse[]>>(`${this.apiUrl}/marketplace/listings`)
      .pipe(map((res) => res.data));
  }

  // Shop side — rates
  publishRate(shopId: number, request: GoldRateRequest): Observable<GoldRateResponse> {
    return this.http
      .post<ApiResponse<GoldRateResponse>>(`${this.apiUrl}/shop/gold-rates/${shopId}`, request)
      .pipe(map((res) => res.data));
  }

  getShopRates(shopId: number): Observable<GoldRateResponse[]> {
    return this.http
      .get<ApiResponse<GoldRateResponse[]>>(`${this.apiUrl}/shop/gold-rates/${shopId}`)
      .pipe(map((res) => res.data));
  }

  // Shop side — offers
  submitOffer(shopId: number, request: GoldOfferRequest): Observable<GoldOfferResponse> {
    return this.http
      .post<ApiResponse<GoldOfferResponse>>(`${this.apiUrl}/shop/offers/${shopId}`, request)
      .pipe(map((res) => res.data));
  }

  getShopOffers(shopId: number): Observable<GoldOfferResponse[]> {
    return this.http
      .get<ApiResponse<GoldOfferResponse[]>>(`${this.apiUrl}/shop/offers/shop/${shopId}`)
      .pipe(map((res) => res.data));
  }

  withdrawShopOffer(offerId: number, shopId: number): Observable<GoldOfferResponse> {
    return this.http
      .put<ApiResponse<GoldOfferResponse>>(`${this.apiUrl}/shop/offers/${offerId}/withdraw`, {}, {
        params: { shopId }
      })
      .pipe(map((res) => res.data));
  }

  // Customer side — listings
  createListing(customerId: number, request: GoldListingRequest): Observable<GoldListingResponse> {
    return this.http
      .post<ApiResponse<GoldListingResponse>>(`${this.apiUrl}/customer/listings/${customerId}`, request)
      .pipe(map((res) => res.data));
  }

  getMyListings(customerId: number): Observable<GoldListingResponse[]> {
    return this.http
      .get<ApiResponse<GoldListingResponse[]>>(`${this.apiUrl}/customer/listings/${customerId}`)
      .pipe(map((res) => res.data));
  }

  getListingDetail(listingId: number): Observable<GoldListingResponse> {
    return this.http
      .get<ApiResponse<GoldListingResponse>>(`${this.apiUrl}/customer/listings/detail/${listingId}`)
      .pipe(map((res) => res.data));
  }

  withdrawListing(listingId: number, customerId: number): Observable<GoldListingResponse> {
    return this.http
      .put<ApiResponse<GoldListingResponse>>(`${this.apiUrl}/customer/listings/${listingId}/withdraw`, {}, {
        params: { customerId }
      })
      .pipe(map((res) => res.data));
  }

  // Customer side — offers
  getOffersForListing(listingId: number): Observable<GoldOfferResponse[]> {
    return this.http
      .get<ApiResponse<GoldOfferResponse[]>>(`${this.apiUrl}/customer/offers/listing/${listingId}`)
      .pipe(map((res) => res.data));
  }

  acceptOffer(offerId: number, customerId: number): Observable<GoldOfferResponse> {
    return this.http
      .put<ApiResponse<GoldOfferResponse>>(`${this.apiUrl}/customer/offers/${offerId}/accept`, {}, {
        params: { customerId }
      })
      .pipe(map((res) => res.data));
  }

  rejectOffer(offerId: number, customerId: number): Observable<GoldOfferResponse> {
    return this.http
      .put<ApiResponse<GoldOfferResponse>>(`${this.apiUrl}/customer/offers/${offerId}/reject`, {}, {
        params: { customerId }
      })
      .pipe(map((res) => res.data));
  }
}