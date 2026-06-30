import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { AuctionResponse, AuctionBidResponse, PlaceBidRequest } from '../models/auction.model';

@Injectable({ providedIn: 'root' })
export class AuctionService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getOpenAuctions(): Observable<AuctionResponse[]> {
    return this.http
      .get<ApiResponse<AuctionResponse[]>>(`${this.apiUrl}/public/auctions`)
      .pipe(map(res => res.data));
  }

  getAuction(id: number): Observable<AuctionResponse> {
    return this.http
      .get<ApiResponse<AuctionResponse>>(`${this.apiUrl}/public/auctions/${id}`)
      .pipe(map(res => res.data));
  }

  getBids(id: number): Observable<AuctionBidResponse[]> {
    return this.http
      .get<ApiResponse<AuctionBidResponse[]>>(`${this.apiUrl}/public/auctions/${id}/bids`)
      .pipe(map(res => res.data));
  }

  placeBid(id: number, request: PlaceBidRequest): Observable<AuctionResponse> {
    return this.http
      .post<ApiResponse<AuctionResponse>>(`${this.apiUrl}/public/auctions/${id}/bids`, request)
      .pipe(map(res => res.data));
  }

  getShopAuctions(shopId: number): Observable<AuctionResponse[]> {
    return this.http
      .get<ApiResponse<AuctionResponse[]>>(`${this.apiUrl}/shop/auctions/${shopId}`)
      .pipe(map(res => res.data));
  }

  startAuction(ticketId: number): Observable<AuctionResponse> {
    return this.http
      .post<ApiResponse<AuctionResponse>>(`${this.apiUrl}/shop/auctions/tickets/${ticketId}/start`, {})
      .pipe(map(res => res.data));
  }

  closeAuction(id: number): Observable<AuctionResponse> {
    return this.http
      .put<ApiResponse<AuctionResponse>>(`${this.apiUrl}/shop/auctions/${id}/close`, {})
      .pipe(map(res => res.data));
  }

  cancelAuction(id: number): Observable<AuctionResponse> {
    return this.http
      .put<ApiResponse<AuctionResponse>>(`${this.apiUrl}/shop/auctions/${id}/cancel`, {})
      .pipe(map(res => res.data));
  }
}