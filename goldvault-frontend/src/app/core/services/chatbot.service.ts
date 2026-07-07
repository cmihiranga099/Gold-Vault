import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth.service';
import { ChatRequest, ChatResponse, ChatTurn } from '../models/chatbot.model';

@Injectable({ providedIn: 'root' })
export class ChatbotService {

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  sendMessage(message: string, history: ChatTurn[]): Observable<ChatResponse> {
    const user = this.authService.currentUser();

    const request: ChatRequest = {
      message,
      history: history.slice(-10), // cap context sent per call
      customerId: user?.customerId ?? null,
      shopId: user?.shopId ?? null,
      portal: this.resolvePortal(user?.role)
    };

    return this.http
      .post<any>(`${environment.apiUrl}/chatbot/message`, request)
      .pipe(map(res => res.data as ChatResponse));
  }

  private resolvePortal(role: string | undefined): ChatRequest['portal'] {
    switch (role) {
      case 'ROLE_ADMIN':      return 'ADMIN';
      case 'ROLE_SHOP_ADMIN':
      case 'ROLE_STAFF':      return 'SHOP';
      case 'ROLE_CUSTOMER':   return 'CUSTOMER';
      default:                return 'PUBLIC';
    }
  }
}