import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';

export interface ShopOption {
  id: number;
  name: string;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class ShopService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /** Active shops only — used to populate the customer registration dropdown */
  getActiveShops(): Observable<ShopOption[]> {
    return this.http
      .get<ApiResponse<ShopOption[]>>(`${this.apiUrl}/shops/active`)
      .pipe(map((res) => res.data));
  }
}