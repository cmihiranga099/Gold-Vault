import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { NotificationFeedResponse } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getCustomerFeed(customerId: number): Observable<NotificationFeedResponse> {
    return this.http
      .get<ApiResponse<NotificationFeedResponse>>(
        `${this.apiUrl}/customer/notifications/${customerId}`)
      .pipe(map(res => res.data));
  }

  markAllCustomerRead(customerId: number): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(
        `${this.apiUrl}/customer/notifications/${customerId}/read-all`, {})
      .pipe(map(res => res.data));
  }

  markReminderRead(customerId: number, notificationId: number): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(
        `${this.apiUrl}/customer/notifications/${customerId}/${notificationId}/read`, {})
      .pipe(map(res => res.data));
  }

  getAdminFeed(): Observable<NotificationFeedResponse> {
    return this.http
      .get<ApiResponse<NotificationFeedResponse>>(`${this.apiUrl}/admin/notifications`)
      .pipe(map(res => res.data));
  }
}