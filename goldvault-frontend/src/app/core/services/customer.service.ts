import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { CustomerRequest, CustomerResponse } from '../models/customer.model';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  register(shopId: number, request: CustomerRequest): Observable<CustomerResponse> {
    return this.http
      .post<ApiResponse<CustomerResponse>>(`${this.apiUrl}/shop/customers/${shopId}`, request)
      .pipe(map((res) => res.data));
  }

  getById(id: number): Observable<CustomerResponse> {
    return this.http
      .get<ApiResponse<CustomerResponse>>(`${this.apiUrl}/shop/customers/${id}`)
      .pipe(map((res) => res.data));
  }

  getByNic(nic: string): Observable<CustomerResponse> {
    return this.http
      .get<ApiResponse<CustomerResponse>>(`${this.apiUrl}/shop/customers/nic/${nic}`)
      .pipe(map((res) => res.data));
  }

  getByShop(shopId: number): Observable<CustomerResponse[]> {
    return this.http
      .get<ApiResponse<CustomerResponse[]>>(`${this.apiUrl}/shop/customers/shop/${shopId}`)
      .pipe(map((res) => res.data));
  }

  search(shopId: number, name: string): Observable<CustomerResponse[]> {
    return this.http
      .get<ApiResponse<CustomerResponse[]>>(`${this.apiUrl}/shop/customers/shop/${shopId}/search`, {
        params: { name }
      })
      .pipe(map((res) => res.data));
  }

  update(id: number, request: CustomerRequest): Observable<CustomerResponse> {
    return this.http
      .put<ApiResponse<CustomerResponse>>(`${this.apiUrl}/shop/customers/${id}`, request)
      .pipe(map((res) => res.data));
  }
}