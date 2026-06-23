import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { ShopDashboardResponse } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class ShopDashboardService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getSummary(shopId: number): Observable<ShopDashboardResponse> {
    return this.http
      .get<ApiResponse<ShopDashboardResponse>>(`${this.apiUrl}/shop/dashboard/${shopId}`)
      .pipe(map((res) => res.data));
  }
}