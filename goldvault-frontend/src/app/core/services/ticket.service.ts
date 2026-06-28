import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { PawnTicketRequest, PawnTicketResponse } from '../models/ticket.model';

export interface RenewalRequest {
  extensionMonths: number;
  interestPaid: number;
  paymentMethod: 'CASH' | 'CARD' | 'ONLINE_TRANSFER' | 'LANKAQR';
  referenceNumber?: string;
}

@Injectable({ providedIn: 'root' })
export class TicketService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getMyTickets(customerId: number): Observable<PawnTicketResponse[]> {
    return this.http
      .get<ApiResponse<PawnTicketResponse[]>>(`${this.apiUrl}/customer/tickets/${customerId}`)
      .pipe(map((res) => res.data));
  }

  getTicketDetail(ticketId: number): Observable<PawnTicketResponse> {
    return this.http
      .get<ApiResponse<PawnTicketResponse>>(`${this.apiUrl}/customer/tickets/detail/${ticketId}`)
      .pipe(map((res) => res.data));
  }

  getShopTickets(shopId: number): Observable<PawnTicketResponse[]> {
    return this.http
      .get<ApiResponse<PawnTicketResponse[]>>(`${this.apiUrl}/shop/tickets/shop/${shopId}`)
      .pipe(map((res) => res.data));
  }

  getShopTicketById(id: number): Observable<PawnTicketResponse> {
    return this.http
      .get<ApiResponse<PawnTicketResponse>>(`${this.apiUrl}/shop/tickets/${id}`)
      .pipe(map((res) => res.data));
  }

  grantTicket(shopId: number, request: PawnTicketRequest): Observable<PawnTicketResponse> {
    return this.http
      .post<ApiResponse<PawnTicketResponse>>(`${this.apiUrl}/shop/tickets/${shopId}`, request)
      .pipe(map((res) => res.data));
  }

  redeemTicket(ticketId: number): Observable<PawnTicketResponse> {
    return this.http
      .put<ApiResponse<PawnTicketResponse>>(`${this.apiUrl}/shop/tickets/${ticketId}/redeem`, {})
      .pipe(map((res) => res.data));
  }

  renewTicket(ticketId: number, request: RenewalRequest): Observable<PawnTicketResponse> {
    return this.http
      .put<ApiResponse<PawnTicketResponse>>(`${this.apiUrl}/shop/tickets/${ticketId}/renew`, request)
      .pipe(map((res) => res.data));
  }

  downloadReceipt(ticketId: number): Observable<HttpResponse<Blob>> {
    return this.http.get(
      `${this.apiUrl}/shop/tickets/${ticketId}/receipt/pdf`,
      { observe: 'response', responseType: 'blob' }
    );
  }
}