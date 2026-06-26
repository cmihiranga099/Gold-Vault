import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import {
  AdminUserResponse, DashboardSummaryResponse,
  RevenueReportResponse, ShopResponse, ShopStatus
} from '../models/admin.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<DashboardSummaryResponse> {
    return this.http
      .get<ApiResponse<DashboardSummaryResponse>>(`${this.apiUrl}/admin/dashboard`)
      .pipe(map((res) => res.data));
  }

  getAllShops(): Observable<ShopResponse[]> {
    return this.http
      .get<ApiResponse<ShopResponse[]>>(`${this.apiUrl}/admin/shops`)
      .pipe(map((res) => res.data));
  }

  getShopsByStatus(status: ShopStatus): Observable<ShopResponse[]> {
    return this.http
      .get<ApiResponse<ShopResponse[]>>(`${this.apiUrl}/admin/shops/status/${status}`)
      .pipe(map((res) => res.data));
  }

  approveShop(id: number): Observable<ShopResponse> {
    return this.http
      .put<ApiResponse<ShopResponse>>(`${this.apiUrl}/admin/shops/${id}/approve`, {})
      .pipe(map((res) => res.data));
  }

  suspendShop(id: number): Observable<ShopResponse> {
    return this.http
      .put<ApiResponse<ShopResponse>>(`${this.apiUrl}/admin/shops/${id}/suspend`, {})
      .pipe(map((res) => res.data));
  }

  reactivateShop(id: number): Observable<ShopResponse> {
    return this.http
      .put<ApiResponse<ShopResponse>>(`${this.apiUrl}/admin/shops/${id}/reactivate`, {})
      .pipe(map((res) => res.data));
  }

  getAllUsers(): Observable<AdminUserResponse[]> {
    return this.http
      .get<ApiResponse<AdminUserResponse[]>>(`${this.apiUrl}/admin/users`)
      .pipe(map((res) => res.data));
  }

  disableUser(id: number): Observable<AdminUserResponse> {
    return this.http
      .put<ApiResponse<AdminUserResponse>>(`${this.apiUrl}/admin/users/${id}/disable`, {})
      .pipe(map((res) => res.data));
  }

  enableUser(id: number): Observable<AdminUserResponse> {
    return this.http
      .put<ApiResponse<AdminUserResponse>>(`${this.apiUrl}/admin/users/${id}/enable`, {})
      .pipe(map((res) => res.data));
  }

  getRevenueReport(startDate: string, endDate: string): Observable<RevenueReportResponse> {
    return this.http
      .get<ApiResponse<RevenueReportResponse>>(`${this.apiUrl}/admin/reports/revenue`, {
        params: { startDate, endDate }
      })
      .pipe(map((res) => res.data));
  }
}