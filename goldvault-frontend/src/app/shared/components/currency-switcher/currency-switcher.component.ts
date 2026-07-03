import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CurrencyService, CURRENCY_OPTIONS, Currency } from '../../../core/services/currency.service';

@Component({
  selector: 'app-currency-switcher',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="currency-switcher" (mouseleave)="open = false">
      <button class="cs-trigger" (click)="open = !open">
        <span>{{ currencyService.currentOption().flag }}</span>
        <span class="cs-code">{{ currencyService.selectedCurrency() }}</span>
        <i class="pi pi-chevron-down cs-chevron" [class.cs-chevron-up]="open"></i>
      </button>

      @if (open) {
        <div class="cs-dropdown">
          @for (option of options; track option.code) {
            <button
              class="cs-option"
              [class.cs-option-active]="currencyService.selectedCurrency() === option.code"
              (click)="select(option.code)"
            >
              <span>{{ option.flag }}</span>
              <span class="cs-option-code">{{ option.code }}</span>
              <span class="cs-option-label">{{ option.label }}</span>
            </button>
          }
          @if (currencyService.ratesError()) {
            <p class="cs-error">Using approximate rates</p>
          }
          @if (currencyService.lastUpdated()) {
            <p class="cs-updated">
              Rates updated {{ currencyService.lastUpdated()! | date:'h:mm a' }}
            </p>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .currency-switcher {
      position: relative;
    }

    .cs-trigger {
      display: flex;
      align-items: center;
      gap: 4px;
      background: none;
      border: 1px solid rgba(201,161,74,0.3);
      border-radius: 6px;
      padding: 4px 8px;
      cursor: pointer;
      font-size: 12px;
      font-weight: 600;
      color: #C9A14A;
      transition: all 0.15s;

      &:hover {
        background: rgba(201,161,74,0.08);
        border-color: #C9A14A;
      }
    }

    .cs-code { font-family: var(--gv-font-mono, monospace); font-size: 11px; }

    .cs-chevron {
      font-size: 9px;
      transition: transform 0.15s;
    }
    .cs-chevron-up { transform: rotate(180deg); }

    .cs-dropdown {
      position: absolute;
      top: calc(100% + 6px);
      right: 0;
      background: #fff;
      border: 1px solid #e0ddd8;
      border-radius: 10px;
      box-shadow: 0 8px 24px rgba(0,0,0,0.12);
      min-width: 200px;
      z-index: 1000;
      padding: 6px;
    }

    .cs-option {
      display: flex;
      align-items: center;
      gap: 8px;
      width: 100%;
      padding: 8px 10px;
      border: none;
      background: none;
      cursor: pointer;
      border-radius: 7px;
      text-align: left;
      transition: background 0.1s;

      &:hover { background: #f8f6f0; }
    }

    .cs-option-active {
      background: rgba(201,161,74,0.1);
      .cs-option-code { color: #C9A14A; }
    }

    .cs-option-code {
      font-size: 12px;
      font-weight: 700;
      color: #333;
      min-width: 36px;
    }

    .cs-option-label {
      font-size: 11px;
      color: #888;
    }

    .cs-error, .cs-updated {
      font-size: 10px;
      color: #aaa;
      text-align: center;
      padding: 4px 8px 2px;
      margin: 0;
    }

    .cs-error { color: #d97706; }
  `]
})
export class CurrencySwitcherComponent {
  options = CURRENCY_OPTIONS;
  open    = false;

  constructor(public currencyService: CurrencyService) {}

  select(currency: Currency): void {
    this.currencyService.setCurrency(currency);
    this.open = false;
  }
}