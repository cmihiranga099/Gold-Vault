import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { ShopAnalyticsResponse } from '../models/analytics.model';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getShopAnalytics(shopId: number): Observable<ShopAnalyticsResponse> {
    return this.http
      .get<ApiResponse<ShopAnalyticsResponse>>(`${this.apiUrl}/shop/analytics/${shopId}`)
      .pipe(map((res) => res.data));
  }
}