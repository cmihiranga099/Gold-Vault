import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { PaymentRequest, PaymentResponse } from '../models/payment.model';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getCustomerPaymentHistory(ticketId: number): Observable<PaymentResponse[]> {
    return this.http
      .get<ApiResponse<PaymentResponse[]>>(`${this.apiUrl}/customer/payments/ticket/${ticketId}`)
      .pipe(map((res) => res.data));
  }

  // Shop-side
  recordPayment(request: PaymentRequest): Observable<PaymentResponse> {
    return this.http
      .post<ApiResponse<PaymentResponse>>(`${this.apiUrl}/shop/payments`, request)
      .pipe(map((res) => res.data));
  }

  getShopPaymentHistory(ticketId: number): Observable<PaymentResponse[]> {
    return this.http
      .get<ApiResponse<PaymentResponse[]>>(`${this.apiUrl}/shop/payments/ticket/${ticketId}`)
      .pipe(map((res) => res.data));
  }
}