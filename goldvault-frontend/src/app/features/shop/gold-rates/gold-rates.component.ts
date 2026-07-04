import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { MessageModule } from 'primeng/message';
import { TagModule } from 'primeng/tag';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../../core/auth/auth.service';
import { MarketplaceService } from '../../../core/services/marketplace.service';
import { MarketRateService } from '../../../core/services/market-rate.service';
import { GoldPurity, GoldRateResponse } from '../../../core/models/marketplace.model';
import { MarketRateResponse, RateComparisonResponse, PURITIES } from '../../../core/models/market-rate.model';

@Component({
  selector: 'app-shop-gold-rates',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    ButtonModule, InputNumberModule, SelectModule, MessageModule, TagModule,
    TranslatePipe
  ],
  templateUrl: './gold-rates.component.html',
  styleUrl: './gold-rates.component.scss'
})
export class GoldRatesComponent implements OnInit {
  rates        = signal<GoldRateResponse[]>([]);
  marketRates  = signal<MarketRateResponse[]>([]);
  comparison   = signal<RateComparisonResponse | null>(null);
  loading      = signal(true);
  publishing   = signal(false);
  errorMessage = signal<string | null>(null);
  publishError = signal<string | null>(null);

  selectedPurity = signal<string>('K22');
  purities = [...PURITIES];
  private shopId: number | null = null;

  form: ReturnType<FormBuilder['group']>;

  constructor(
    private fb:                FormBuilder,
    private authService:       AuthService,
    private marketplaceService: MarketplaceService,
    private marketRateService:  MarketRateService
  ) {
    this.form = this.fb.group({
      purity:      ['K22', Validators.required],
      ratePerGram: [null as number | null, [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    this.shopId = this.authService.currentUser()?.shopId ?? null;
    if (!this.shopId) { this.errorMessage.set('No shop linked.'); this.loading.set(false); return; }
    this.loadRates();
    this.loadMarketRates();
    this.loadComparison('K22');
  }

  loadRates(): void {
    if (!this.shopId) return;
    this.loading.set(true);
    this.marketplaceService.getShopRates(this.shopId).subscribe({
      next:  (rates) => { this.rates.set(rates); this.loading.set(false); },
      error: ()      => { this.errorMessage.set('Could not load rates.'); this.loading.set(false); }
    });
  }

  loadMarketRates(): void {
    this.marketRateService.getLatestMarketRates().subscribe({
      next:  (rates) => this.marketRates.set(rates),
      error: ()      => {}
    });
  }

  loadComparison(purity: string): void {
    this.selectedPurity.set(purity);
    this.marketRateService.getComparison(purity).subscribe({
      next:  (c) => this.comparison.set(c),
      error: ()  => this.comparison.set(null)
    });
  }

  onPublish(): void {
    if (this.form.invalid || !this.shopId) { this.form.markAllAsTouched(); return; }
    this.publishing.set(true);
    this.publishError.set(null);
    const raw = this.form.getRawValue();

    this.marketplaceService.publishRate(this.shopId, {
      purity:      raw.purity,
      ratePerGram: raw.ratePerGram
    }).subscribe({
      next: () => {
        this.publishing.set(false);
        this.form.patchValue({ ratePerGram: null });
        this.loadRates();
        this.loadComparison(raw.purity);
      },
      error: (err) => {
        this.publishing.set(false);
        this.publishError.set(err?.error?.message || 'Could not publish rate.');
      }
    });
  }

  marketRateForPurity(purity: string): number | null {
    return this.marketRates().find(r => r.purity === purity)?.ratePerGram ?? null;
  }

  vsMarket(shopRate: number, purity: string): string | null {
    const market = this.marketRateForPurity(purity);
    if (!market) return null;
    const diff = ((shopRate - market) / market * 100).toFixed(1);
    return `${Number(diff) >= 0 ? '+' : ''}${diff}% vs market`;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }
}