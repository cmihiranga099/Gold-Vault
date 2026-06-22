import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';

const TOKEN_KEY = 'goldvault_token';
const USER_KEY = 'goldvault_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;

  /** Reactive signal so components can react to login/logout without manual subscriptions */
  currentUser = signal<AuthResponse | null>(this.loadUserFromStorage());

  constructor(private http: HttpClient, private router: Router) {}

  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/login`, request).pipe(
      tap((res) => this.setSession(res.data))
    );
  }

  register(request: RegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/register`, request).pipe(
      tap((res) => this.setSession(res.data))
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  hasRole(...roles: string[]): boolean {
    const user = this.currentUser();
    return !!user && roles.includes(user.role);
  }

  private setSession(auth: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, auth.token);
    localStorage.setItem(USER_KEY, JSON.stringify(auth));
    this.currentUser.set(auth);
  }

  private loadUserFromStorage(): AuthResponse | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }
}