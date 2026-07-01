import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { MarketRateResponse, RateComparisonResponse } from '../models/market-rate.model';

@Injectable({ providedIn: 'root' })
export class MarketRateService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getLatestMarketRates(): Observable<MarketRateResponse[]> {
    return this.http
      .get<ApiResponse<MarketRateResponse[]>>(`${this.apiUrl}/public/market-rates`)
      .pipe(map(res => res.data));
  }

  getRateHistory(purity: string): Observable<MarketRateResponse[]> {
    return this.http
      .get<ApiResponse<MarketRateResponse[]>>(`${this.apiUrl}/public/market-rates/${purity}/history`)
      .pipe(map(res => res.data));
  }

  getComparison(purity: string): Observable<RateComparisonResponse> {
    return this.http
      .get<ApiResponse<RateComparisonResponse>>(`${this.apiUrl}/public/market-rates/${purity}/compare`)
      .pipe(map(res => res.data));
  }

  publishMarketRate(purity: string, ratePerGram: number): Observable<MarketRateResponse> {
    return this.http
      .post<ApiResponse<MarketRateResponse>>(`${this.apiUrl}/admin/market-rates`, { purity, ratePerGram })
      .pipe(map(res => res.data));
  }
}