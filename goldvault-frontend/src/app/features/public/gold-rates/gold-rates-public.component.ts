import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { MarketRateService } from '../../../core/services/market-rate.service';
import { MarketRateResponse, RateComparisonResponse, PURITIES } from '../../../core/models/market-rate.model';
import { CurrencyConvertPipe } from '../../../core/pipes/currency-convert.pipe';
import { CurrencyService } from '../../../core/services/currency.service';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-gold-rates-public',
  standalone: true,
  imports: [CommonModule, RouterLink, ProgressSpinnerModule, CurrencyConvertPipe, TranslatePipe],
  templateUrl: './gold-rates-public.component.html',
  styleUrl:    './gold-rates-public.component.scss',
})
export class GoldRatesPublicComponent implements OnInit {
  marketRates  = signal<MarketRateResponse[]>([]);
  comparison   = signal<RateComparisonResponse | null>(null);
  history      = signal<MarketRateResponse[]>([]);
  loading      = signal(true);
  selectedPurity = signal<string>('K22');
  purities = [...PURITIES];

  constructor(
    private marketRateService: MarketRateService,
    public  currencyService:   CurrencyService
  ) {}

  ngOnInit(): void {
    this.marketRateService.getLatestMarketRates().subscribe({
      next: (rates) => { this.marketRates.set(rates); this.loading.set(false); },
      error: ()     => this.loading.set(false)
    });
    this.loadComparison('K22');
    this.loadHistory('K22');
  }

  loadComparison(purity: string): void {
    this.selectedPurity.set(purity);
    this.marketRateService.getComparison(purity).subscribe({
      next: (c) => this.comparison.set(c),
      error: ()  => this.comparison.set(null)
    });
    this.loadHistory(purity);
  }

  loadHistory(purity: string): void {
    this.marketRateService.getRateHistory(purity).subscribe({
      next: (h) => this.history.set(h),
      error: ()  => this.history.set([])
    });
  }

  maxRate(): number {
    return Math.max(...this.history().map(h => h.ratePerGram), 1);
  }

  lowRate(): number {
    const rates = this.history().map(h => h.ratePerGram);
    return rates.length ? Math.min(...rates) : 0;
  }

  highRate(): number {
    const rates = this.history().map(h => h.ratePerGram);
    return rates.length ? Math.max(...rates) : 0;
  }

  barHeight(rate: number): string {
    const max = this.maxRate();
    return Math.max(4, Math.round((rate / max) * 100)) + 'px';
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }

  shortDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('en-LK', { month: 'short', day: 'numeric' });
  }
}