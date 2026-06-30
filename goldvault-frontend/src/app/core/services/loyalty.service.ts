import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { LoyaltySummaryResponse, RedeemPointsRequest } from '../models/loyalty.model';

@Injectable({ providedIn: 'root' })
export class LoyaltyService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getSummary(customerId: number): Observable<LoyaltySummaryResponse> {
    return this.http
      .get<ApiResponse<LoyaltySummaryResponse>>(`${this.apiUrl}/customer/loyalty/${customerId}`)
      .pipe(map(res => res.data));
  }

  redeemPoints(request: RedeemPointsRequest): Observable<number> {
    return this.http
      .post<ApiResponse<number>>(`${this.apiUrl}/shop/loyalty/redeem`, request)
      .pipe(map(res => res.data));
  }
}