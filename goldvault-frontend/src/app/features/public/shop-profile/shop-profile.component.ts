import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ShopProfileService } from '../../../core/services/shop-profile.service';
import { ShopProfileResponse } from '../../../core/models/shop-profile.model';
import { CurrencyConvertPipe } from '../../../core/pipes/currency-convert.pipe';

@Component({
  selector: 'app-shop-profile',
  standalone: true,
  imports: [CommonModule, RouterLink, TagModule, ProgressSpinnerModule, CurrencyConvertPipe],
  templateUrl: './shop-profile.component.html',
  styleUrl:    './shop-profile.component.scss'
})
export class ShopProfileComponent implements OnInit {
  profile  = signal<ShopProfileResponse | null>(null);
  loading  = signal(true);
  error    = signal<string | null>(null);

  constructor(
    private route:              ActivatedRoute,
    private shopProfileService: ShopProfileService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) { this.error.set('Invalid shop.'); this.loading.set(false); return; }

    this.shopProfileService.getProfile(id).subscribe({
      next:  (p) => { this.profile.set(p); this.loading.set(false); },
      error: ()  => { this.error.set('Could not load this shop\'s profile.'); this.loading.set(false); }
    });
  }

  starsArray(n: number): number[] {
    return Array.from({ length: 5 }, (_, i) => i + 1);
  }

  ratingBarWidth(purity: number): string {
    const p = this.profile();
    if (!p) return '0%';
    const max = Math.max(...Object.values(p.ratingDistribution), 1);
    return Math.round((p.ratingDistribution[purity] / max) * 100) + '%';
  }

  goldRateKeys(): string[] {
    return Object.keys(this.profile()?.goldRates ?? {});
  }

  promoTypeLabel(type: string): string {
    const map: Record<string, string> = {
      REDUCED_INTEREST: '🏷️ Reduced interest',
      BONUS_POINTS:     '⭐ Bonus points',
      FREE_RENEWAL:     '🔄 Free renewal',
      CUSTOM:           '🎁 Special offer'
    };
    return map[type] ?? type;
  }

  shareUrl(): string {
    return window.location.href;
  }

  copyShareUrl(): void {
    navigator.clipboard.writeText(this.shareUrl());
  }

  formatCurrency(n: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(n);
  }
}