import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export type Currency = 'LKR' | 'USD' | 'AED' | 'GBP' | 'EUR';

export interface CurrencyOption {
  code:   Currency;
  label:  string;
  symbol: string;
  flag:   string;
}

export const CURRENCY_OPTIONS: CurrencyOption[] = [
  { code: 'LKR', label: 'Sri Lankan Rupee', symbol: 'Rs',  flag: '🇱🇰' },
  { code: 'USD', label: 'US Dollar',        symbol: '$',   flag: '🇺🇸' },
  { code: 'AED', label: 'UAE Dirham',       symbol: 'AED', flag: '🇦🇪' },
  { code: 'GBP', label: 'British Pound',    symbol: '£',   flag: '🇬🇧' },
  { code: 'EUR', label: 'Euro',             symbol: '€',   flag: '🇪🇺' }
];

@Injectable({ providedIn: 'root' })
export class CurrencyService {
  private readonly STORAGE_KEY = 'gv_currency';

  // Signals
  selectedCurrency = signal<Currency>('LKR');
  rates            = signal<Record<string, number>>({ LKR: 1 });
  ratesLoading     = signal(true);
  ratesError       = signal(false);
  lastUpdated      = signal<Date | null>(null);

  constructor(private http: HttpClient) {
    const saved = localStorage.getItem(this.STORAGE_KEY) as Currency | null;
    if (saved && CURRENCY_OPTIONS.find(c => c.code === saved)) {
      this.selectedCurrency.set(saved);
    }
    this.loadRates();
  }

  // ── Load exchange rates from free public API ───────────────────────────────

  private loadRates(): void {
    this.ratesLoading.set(true);
    // Free open API — no key needed, returns rates relative to USD
    this.http.get<any>('https://open.er-api.com/v6/latest/LKR').subscribe({
      next: (res) => {
        if (res?.rates) {
          this.rates.set(res.rates);
          this.lastUpdated.set(new Date());
        }
        this.ratesLoading.set(false);
      },
      error: () => {
        // Fallback to hardcoded approximate rates if API fails
        this.rates.set({
          LKR: 1,
          USD: 0.0033,   // 1 LKR ≈ 0.0033 USD
          AED: 0.012,    // 1 LKR ≈ 0.012 AED
          GBP: 0.0026,   // 1 LKR ≈ 0.0026 GBP
          EUR: 0.003     // 1 LKR ≈ 0.003 EUR
        });
        this.ratesError.set(true);
        this.ratesLoading.set(false);
      }
    });
  }

  // ── Set currency ──────────────────────────────────────────────────────────

  setCurrency(currency: Currency): void {
    this.selectedCurrency.set(currency);
    localStorage.setItem(this.STORAGE_KEY, currency);
  }

  // ── Convert LKR to selected currency ─────────────────────────────────────

  convert(lkrAmount: number): number {
    const currency = this.selectedCurrency();
    if (currency === 'LKR') return lkrAmount;
    const rate = this.rates()[currency];
    if (!rate) return lkrAmount;
    return lkrAmount * rate;
  }

  // ── Format with symbol ────────────────────────────────────────────────────

  format(lkrAmount: number, decimals = 2): string {
    const currency  = this.selectedCurrency();
    const option    = CURRENCY_OPTIONS.find(c => c.code === currency)!;
    const converted = this.convert(lkrAmount);

    const formatted = new Intl.NumberFormat('en-LK', {
      minimumFractionDigits: decimals,
      maximumFractionDigits: decimals
    }).format(converted);

    return `${option.symbol} ${formatted}`;
  }

  // ── Current option ────────────────────────────────────────────────────────

  currentOption(): CurrencyOption {
    return CURRENCY_OPTIONS.find(c => c.code === this.selectedCurrency())
        ?? CURRENCY_OPTIONS[0];
  }
}