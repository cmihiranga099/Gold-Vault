import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { PaymentRequest, PaymentResponse, PaymentSubmissionResponse, PaymentType } from '../models/payment.model';

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

  // ── Online repayment submissions (bank transfer + receipt) ──────────────────

  submitOnlinePayment(customerId: number, payload: {
    ticketId: number;
    amount: number;
    paymentType: PaymentType;
    bankName?: string;
    referenceNumber: string;
    receipt: File;
  }): Observable<PaymentSubmissionResponse> {
    const form = new FormData();
    form.append('ticketId', String(payload.ticketId));
    form.append('amount', String(payload.amount));
    form.append('paymentType', payload.paymentType);
    if (payload.bankName) form.append('bankName', payload.bankName);
    form.append('referenceNumber', payload.referenceNumber);
    form.append('receipt', payload.receipt);

    return this.http
      .post<ApiResponse<PaymentSubmissionResponse>>(
        `${this.apiUrl}/customer/payments/submissions/${customerId}`, form)
      .pipe(map((res) => res.data));
  }

  getSubmissionsForTicket(ticketId: number, asShop = false): Observable<PaymentSubmissionResponse[]> {
    const base = asShop ? 'shop' : 'customer';
    return this.http
      .get<ApiResponse<PaymentSubmissionResponse[]>>(
        `${this.apiUrl}/${base}/payments/submissions/ticket/${ticketId}`)
      .pipe(map((res) => res.data));
  }

  getPendingSubmissionsForShop(shopId: number): Observable<PaymentSubmissionResponse[]> {
    return this.http
      .get<ApiResponse<PaymentSubmissionResponse[]>>(
        `${this.apiUrl}/shop/payments/submissions/pending/${shopId}`)
      .pipe(map((res) => res.data));
  }

  approveSubmission(id: number, reviewedBy?: number): Observable<PaymentSubmissionResponse> {
    const params: Record<string, string> = reviewedBy != null ? { reviewedBy: String(reviewedBy) } : {};
    return this.http
      .post<ApiResponse<PaymentSubmissionResponse>>(
        `${this.apiUrl}/shop/payments/submissions/${id}/approve`, {}, { params })
      .pipe(map((res) => res.data));
  }

  rejectSubmission(id: number, reason: string, reviewedBy?: number): Observable<PaymentSubmissionResponse> {
    const params: Record<string, string> = reviewedBy != null ? { reviewedBy: String(reviewedBy) } : {};
    return this.http
      .post<ApiResponse<PaymentSubmissionResponse>>(
        `${this.apiUrl}/shop/payments/submissions/${id}/reject`, { reason }, { params })
      .pipe(map((res) => res.data));
  }
}