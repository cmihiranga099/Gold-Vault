import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { ShopProfileResponse } from '../models/shop-profile.model';

@Injectable({ providedIn: 'root' })
export class ShopProfileService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getProfile(shopId: number): Observable<ShopProfileResponse> {
    return this.http
      .get<ApiResponse<ShopProfileResponse>>(`${this.apiUrl}/shops/${shopId}/profile`)
      .pipe(map(res => res.data));
  }
}