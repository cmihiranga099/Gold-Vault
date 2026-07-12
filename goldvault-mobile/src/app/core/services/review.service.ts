import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { ReviewRequest, ReviewResponse, ShopRatingResponse } from '../models/review.model';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  submitReview(customerId: number, request: ReviewRequest): Observable<ReviewResponse> {
    return this.http
      .post<ApiResponse<ReviewResponse>>(
        `${this.apiUrl}/customer/reviews?customerId=${customerId}`, request)
      .pipe(map(res => res.data));
  }

  hasReviewed(ticketId: number): Observable<boolean> {
    return this.http
      .get<ApiResponse<boolean>>(`${this.apiUrl}/customer/reviews/check/${ticketId}`)
      .pipe(map(res => res.data));
  }

  getShopReviews(shopId: number): Observable<ReviewResponse[]> {
    return this.http
      .get<ApiResponse<ReviewResponse[]>>(`${this.apiUrl}/public/shops/${shopId}/reviews`)
      .pipe(map(res => res.data));
  }

  getShopRating(shopId: number): Observable<ShopRatingResponse> {
    return this.http
      .get<ApiResponse<ShopRatingResponse>>(`${this.apiUrl}/public/shops/${shopId}/rating`)
      .pipe(map(res => res.data));
  }

  getMyShopReviews(shopId: number): Observable<ReviewResponse[]> {
    return this.http
      .get<ApiResponse<ReviewResponse[]>>(`${this.apiUrl}/shop/reviews/${shopId}`)
      .pipe(map(res => res.data));
  }
}