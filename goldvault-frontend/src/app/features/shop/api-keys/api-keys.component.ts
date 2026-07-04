import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TooltipModule } from 'primeng/tooltip';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
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
  imports: [CommonModule, RouterLink, ButtonModule, MessageModule, TagModule, ProgressSpinnerModule, TooltipModule, TranslatePipe],
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
    private http:        HttpClient,
    private translate:   TranslateService
  ) {}

  ngOnInit(): void {
    this.shopId = this.authService.currentUser()?.shopId!;
    this.loadKeys();
  }

  private loadKeys(): void {
    this.loading.set(true);
    this.http.get<any>(`${environment.apiUrl}/shop/api-keys/${this.shopId}`).subscribe({
      next:  (res) => { this.keys.set(res.data); this.loading.set(false); },
      error: ()    => { this.error.set(this.translate.instant('apiKeys.errLoad')); this.loading.set(false); }
    });
  }

  generateKey(): void {
    if (!confirm(this.translate.instant('apiKeys.confirmGenerate'))) return;
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
      error: () => { this.generating.set(false); this.error.set(this.translate.instant('apiKeys.errGenerate')); }
    });
  }

  revokeKey(keyId: number): void {
    if (!confirm(this.translate.instant('apiKeys.confirmRevoke'))) return;
    this.http.delete<any>(`${environment.apiUrl}/shop/api-keys/${this.shopId}/revoke/${keyId}`).subscribe({
      next:  () => { this.newKey.set(null); this.loadKeys(); },
      error: () => this.error.set(this.translate.instant('apiKeys.errRevoke'))
    });
  }

  copyKey(): void {
    if (this.newKey()) {
      navigator.clipboard.writeText(this.newKey()!);
      alert(this.translate.instant('apiKeys.copiedAlert'));
    }
  }
}