import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { ShopResponse } from '../models/shop.model';

@Injectable({ providedIn: 'root' })
export class ShopService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /** All active shops — used by both the registration dropdown and the shop finder */
  getActiveShops(): Observable<ShopResponse[]> {
    return this.http
      .get<ApiResponse<ShopResponse[]>>(`${this.apiUrl}/shops/active`)
      .pipe(map((res) => res.data));
  }
}