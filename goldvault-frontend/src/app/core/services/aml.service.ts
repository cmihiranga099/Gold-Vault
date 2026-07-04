import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { AmlFlagResponse, AmlSummary, AmlReviewRequest } from '../models/aml.model';

@Injectable({ providedIn: 'root' })
export class AmlService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAllFlags(openOnly = false): Observable<AmlFlagResponse[]> {
    return this.http
      .get<ApiResponse<AmlFlagResponse[]>>(
        `${this.apiUrl}/admin/aml/flags?openOnly=${openOnly}`)
      .pipe(map(res => res.data));
  }

  getSummary(): Observable<AmlSummary> {
    return this.http
      .get<ApiResponse<AmlSummary>>(`${this.apiUrl}/admin/aml/summary`)
      .pipe(map(res => res.data));
  }

  reviewFlag(flagId: number, request: AmlReviewRequest): Observable<AmlFlagResponse> {
    return this.http
      .put<ApiResponse<AmlFlagResponse>>(
        `${this.apiUrl}/admin/aml/flags/${flagId}/review`, request)
      .pipe(map(res => res.data));
  }

  triggerScan(): Observable<string> {
    return this.http
      .post<ApiResponse<string>>(`${this.apiUrl}/admin/aml/scan`, {})
      .pipe(map(res => res.data));
  }
}