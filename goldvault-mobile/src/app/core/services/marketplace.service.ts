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