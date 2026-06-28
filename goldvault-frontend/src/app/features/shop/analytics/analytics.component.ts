import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AuthService } from '../../../core/auth/auth.service';
import { AnalyticsService } from '../../../core/services/analytics.service';
import { ShopAnalyticsResponse } from '../../../core/models/analytics.model';

@Component({
  selector: 'app-shop-analytics',
  standalone: true,
  imports: [CommonModule, RouterLink, ProgressSpinnerModule, TopnavComponent],
  templateUrl: './analytics.component.html',
  styleUrl:    './analytics.component.scss'
})
export class ShopAnalyticsComponent implements OnInit {
  data    = signal<ShopAnalyticsResponse | null>(null);
  loading = signal(true);
  error   = signal<string | null>(null);

  constructor(
    private authService:     AuthService,
    private analyticsService: AnalyticsService
  ) {}

  ngOnInit(): void {
    const shopId = this.authService.currentUser()?.shopId;
    if (!shopId) { this.error.set('No shop linked.'); this.loading.set(false); return; }

    this.analyticsService.getShopAnalytics(shopId).subscribe({
      next:  (d) => { this.data.set(d); this.loading.set(false); },
      error: ()  => { this.error.set('Could not load analytics.'); this.loading.set(false); }
    });
  }

  // ── Helpers ──────────────────────────────────────────────────────────────────

  lkr(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount ?? 0);
  }

  pct(val: number): string {
    return (val ?? 0).toFixed(1) + '%';
  }

  months(map: Record<string, number> | Record<string, number>): string[] {
    return Object.keys(map ?? {});
  }

  shortMonth(ym: string): string {
    const [y, m] = ym.split('-');
    return new Date(+y, +m - 1, 1).toLocaleString('en-LK', { month: 'short' }) + ' ' + y.slice(2);
  }

  barHeight(val: number, max: number): string {
    if (!max || max === 0) return '4px';
    return Math.max(4, Math.round((val / max) * 140)) + 'px';
  }

  maxVal(map: Record<string, number>): number {
    return Math.max(...Object.values(map ?? {}), 1);
  }

  momArrow(pct: number): string {
    return pct >= 0 ? '▲' : '▼';
  }

  momColor(pct: number): string {
    return pct >= 0 ? '#16a34a' : '#dc2626';
  }

  collectionBreakdownItems(d: ShopAnalyticsResponse): { label: string; amount: number; color: string }[] {
    const total = (d.thisMonthInterest ?? 0) + (d.thisMonthPartial ?? 0)
                + (d.thisMonthRedemptions ?? 0) + (d.thisMonthRenewals ?? 0);
    return [
      { label: 'Interest',    amount: d.thisMonthInterest,    color: '#3B8BD4' },
      { label: 'Partial',     amount: d.thisMonthPartial,     color: '#C9A14A' },
      { label: 'Redemptions', amount: d.thisMonthRedemptions, color: '#1D9E75' },
      { label: 'Renewals',    amount: d.thisMonthRenewals,    color: '#7F77DD' }
    ];
  }

  breakdownBarWidth(amount: number, d: ShopAnalyticsResponse): string {
    const total = (d.thisMonthInterest ?? 0) + (d.thisMonthPartial ?? 0)
                + (d.thisMonthRedemptions ?? 0) + (d.thisMonthRenewals ?? 0);
    if (!total) return '0%';
    return Math.round((amount / total) * 100) + '%';
  }
}