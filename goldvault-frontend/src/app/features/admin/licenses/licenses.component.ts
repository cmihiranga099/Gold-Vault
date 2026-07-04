import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { MessageModule } from 'primeng/message';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ShopResponse } from '../../../core/models/shop.model';

@Component({
  selector: 'app-license-verification',
  standalone: true,
  imports: [
    CommonModule, RouterLink, FormsModule,
    TagModule, ButtonModule, DialogModule, ProgressSpinnerModule, MessageModule,
    TopnavComponent
  ],
  templateUrl: './licenses.component.html',
  styleUrl:    './licenses.component.scss'
})
export class LicenseVerificationComponent implements OnInit {
  shops    = signal<ShopResponse[]>([]);
  loading  = signal(true);
  filter   = signal<'ALL' | 'PENDING' | 'VERIFIED' | 'REJECTED'>('PENDING');

  selectedShop  = signal<ShopResponse | null>(null);
  showReject    = false;
  rejectReason  = '';
  actionLoading = signal(false);
  successMsg    = signal<string | null>(null);

  constructor(private http: HttpClient) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.http.get<any>(`${environment.apiUrl}/admin/licenses`).subscribe({
      next:  (res) => { this.shops.set(res.data); this.loading.set(false); },
      error: ()    => this.loading.set(false)
    });
  }

  filtered(): ShopResponse[] {
    const f = this.filter();
    if (f === 'ALL') return this.shops();
    return this.shops().filter(s => s.licenseStatus === f);
  }

  verify(shop: ShopResponse): void {
    if (!confirm(`Verify license for ${shop.name}? This will activate the shop.`)) return;
    this.actionLoading.set(true);
    this.http.put<any>(
      `${environment.apiUrl}/admin/licenses/${shop.id}/verify?verifiedBy=Admin`, {}
    ).subscribe({
      next: () => {
        this.actionLoading.set(false);
        this.successMsg.set(`${shop.name} license verified and shop activated.`);
        this.load();
        setTimeout(() => this.successMsg.set(null), 3000);
      },
      error: () => this.actionLoading.set(false)
    });
  }

  openReject(shop: ShopResponse): void {
    this.selectedShop.set(shop);
    this.rejectReason = '';
    this.showReject   = true;
  }

  submitReject(): void {
    const shop = this.selectedShop();
    if (!shop || !this.rejectReason.trim()) return;
    this.actionLoading.set(true);

    this.http.put<any>(
      `${environment.apiUrl}/admin/licenses/${shop.id}/reject`,
      { reason: this.rejectReason, reviewedBy: 'Admin' }
    ).subscribe({
      next: () => {
        this.actionLoading.set(false);
        this.showReject = false;
        this.successMsg.set(`License rejected for ${shop.name}.`);
        this.load();
        setTimeout(() => this.successMsg.set(null), 3000);
      },
      error: () => this.actionLoading.set(false)
    });
  }

  licenseSeverity(status: string): 'success' | 'warn' | 'danger' | 'secondary' {
    if (status === 'VERIFIED')  return 'success';
    if (status === 'PENDING')   return 'warn';
    if (status === 'REJECTED')  return 'danger';
    return 'secondary';
  }

  openDocument(url: string): void {
    window.open('/' + url, '_blank');
  }
}