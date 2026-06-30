import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { ShopResponse } from '../models/shop.model';

export interface ShopOption {
  id: number;
  name: string;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class ShopService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /** Active shops, lightweight — used to populate the customer registration dropdown */
  getActiveShopOptions(): Observable<ShopOption[]> {
    return this.http
      .get<ApiResponse<ShopOption[]>>(`${this.apiUrl}/shops/active`)
      .pipe(map((res) => res.data));
  }

  /** Active shops, full detail including lat/lng + rating — used by the shop finder map */
  getActiveShops(): Observable<ShopResponse[]> {
    return this.http
      .get<ApiResponse<ShopResponse[]>>(`${this.apiUrl}/shops/active`)
      .pipe(map((res) => res.data));
  }

  updateLocation(shopId: number, lat: number, lng: number): Observable<ShopResponse> {
    return this.http
      .put<ApiResponse<ShopResponse>>(
        `${this.apiUrl}/shop/dashboard/${shopId}/location?latitude=${lat}&longitude=${lng}`, {})
      .pipe(map((res) => res.data));
  }
}