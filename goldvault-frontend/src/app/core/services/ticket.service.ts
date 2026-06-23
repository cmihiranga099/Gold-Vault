import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { PawnTicketResponse } from '../models/ticket.model';

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

  // Shop-side
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
}