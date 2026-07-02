import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AuthService } from '../../../core/auth/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

interface ApiKeyInfo {
  id:          number;
  keyPreview:  string;
  label:       string;
  enabled:     boolean;
  lastUsed:    string | null;
  createdAt:   string;
}

@Component({
  selector: 'app-api-keys',
  standalone: true,
  imports: [CommonModule, RouterLink, ButtonModule, MessageModule, TagModule, ProgressSpinnerModule, TopnavComponent],
  templateUrl: './api-keys.component.html',
  styleUrl:    './api-keys.component.scss'
})
export class ApiKeysComponent implements OnInit {
  keys        = signal<ApiKeyInfo[]>([]);
  loading     = signal(true);
  generating  = signal(false);
  newKey      = signal<string | null>(null);
  error       = signal<string | null>(null);

  private shopId!: number;

  constructor(
    private authService: AuthService,
    private http:        HttpClient
  ) {}

  ngOnInit(): void {
    this.shopId = this.authService.currentUser()?.shopId!;
    this.loadKeys();
  }

  private loadKeys(): void {
    this.loading.set(true);
    this.http.get<any>(`${environment.apiUrl}/shop/api-keys/${this.shopId}`).subscribe({
      next:  (res) => { this.keys.set(res.data); this.loading.set(false); },
      error: ()    => { this.error.set('Could not load API keys.'); this.loading.set(false); }
    });
  }

  generateKey(): void {
    if (!confirm('Generate a new API key? You will see it only once.')) return;
    this.generating.set(true);
    this.newKey.set(null);
    this.http.post<any>(
      `${environment.apiUrl}/shop/api-keys/${this.shopId}/generate?label=POS Integration`, {}
    ).subscribe({
      next: (res) => {
        this.generating.set(false);
        this.newKey.set(res.data.apiKey);
        this.loadKeys();
      },
      error: () => { this.generating.set(false); this.error.set('Could not generate key.'); }
    });
  }

  revokeKey(keyId: number): void {
    if (!confirm('Revoke this API key? Any POS using it will stop working immediately.')) return;
    this.http.delete<any>(`${environment.apiUrl}/shop/api-keys/${this.shopId}/revoke/${keyId}`).subscribe({
      next:  () => { this.newKey.set(null); this.loadKeys(); },
      error: () => this.error.set('Could not revoke key.')
    });
  }

  copyKey(): void {
    if (this.newKey()) {
      navigator.clipboard.writeText(this.newKey()!);
      alert('API key copied to clipboard!');
    }
  }
}