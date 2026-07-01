import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { PromotionRequest, PromotionResponse } from '../models/promotion.model';

@Injectable({ providedIn: 'root' })
export class PromotionService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  createPromotion(shopId: number, request: PromotionRequest): Observable<PromotionResponse> {
    return this.http
      .post<ApiResponse<PromotionResponse>>(`${this.apiUrl}/shop/promotions/${shopId}`, request)
      .pipe(map(res => res.data));
  }

  getShopPromotions(shopId: number): Observable<PromotionResponse[]> {
    return this.http
      .get<ApiResponse<PromotionResponse[]>>(`${this.apiUrl}/shop/promotions/${shopId}`)
      .pipe(map(res => res.data));
  }

  cancelPromotion(promoId: number, shopId: number): Observable<PromotionResponse> {
    return this.http
      .put<ApiResponse<PromotionResponse>>(`${this.apiUrl}/shop/promotions/${promoId}/cancel/${shopId}`, {})
      .pipe(map(res => res.data));
  }

  getAllActivePromotions(): Observable<PromotionResponse[]> {
    return this.http
      .get<ApiResponse<PromotionResponse[]>>(`${this.apiUrl}/public/promotions`)
      .pipe(map(res => res.data));
  }

  getActiveByShop(shopId: number): Observable<PromotionResponse[]> {
    return this.http
      .get<ApiResponse<PromotionResponse[]>>(`${this.apiUrl}/public/promotions/shop/${shopId}`)
      .pipe(map(res => res.data));
  }
}