import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, IonContent,
  IonItem, IonInput, IonSelect, IonSelectOption, IonSegment, IonSegmentButton,
  IonLabel, IonList
} from '@ionic/angular/standalone';

interface AmortisationRow {
  month: number;
  openingBalance: number;
  interest: number;
  payment: number;
  closingBalance: number;
}

// Approximate reference rates (LKR per gram) used only to illustrate loan-to-value —
// actual live rates are shown in the Marketplace tab.
const REFERENCE_RATES: Record<string, number> = {
  K24: 18500, K22: 17000, K21: 16200, K18: 13900,
  P916: 16950, P750: 13875, OTHER: 12000
};

@Component({
  selector: 'app-calculator',
  standalone: true,
  imports: [
    CommonModule,
    IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, IonContent,
    IonItem, IonInput, IonSelect, IonSelectOption, IonSegment, IonSegmentButton,
    IonLabel, IonList
  ],
  templateUrl: './calculator.page.html',
  styleUrl: './calculator.page.scss'
})
export class CalculatorPage {
  loanAmount = signal(50000);
  interestRate = signal(2.5); // % per month
  periodMonths = signal(6);
  interestType = signal<'FLAT' | 'REDUCING'>('FLAT');
  goldWeight = signal(10); // grams
  goldPurity = signal('K22');

  purities = ['K24', 'K22', 'K21', 'K18', 'P916', 'P750', 'OTHER'];

  schedule = computed<AmortisationRow[]>(() => {
    const principal = this.loanAmount();
    const rate = this.interestRate() / 100;
    const months = this.periodMonths();
    const type = this.interestType();
    const rows: AmortisationRow[] = [];

    if (type === 'FLAT') {
      const monthlyInterest = principal * rate;
      for (let m = 1; m <= months; m++) {
        const isLast = m === months;
        rows.push({
          month: m,
          openingBalance: principal,
          interest: monthlyInterest,
          payment: isLast ? monthlyInterest + principal : monthlyInterest,
          closingBalance: isLast ? 0 : principal
        });
      }
    } else {
      let balance = principal;
      for (let m = 1; m <= months; m++) {
        const interest = balance * rate;
        const principalPayment = principal / months;
        const payment = interest + principalPayment;
        const closing = Math.max(0, balance - principalPayment);
        rows.push({ month: m, openingBalance: balance, interest, payment, closingBalance: closing });
        balance = closing;
      }
    }
    return rows;
  });

  totalInterest = computed(() => this.schedule().reduce((sum, r) => sum + r.interest, 0));
  totalPayable = computed(() => this.loanAmount() + this.totalInterest());
  monthlyPayment = computed(() => this.schedule()[0]?.payment ?? 0);

  effectiveCostPct = computed(() => {
    const principal = this.loanAmount();
    if (!principal) return 0;
    return (this.totalInterest() / principal) * 100;
  });

  loanToValueRatio = computed(() => {
    const ratePerGram = REFERENCE_RATES[this.goldPurity()] ?? 12000;
    const goldValue = this.goldWeight() * ratePerGram;
    if (!goldValue) return 0;
    return (this.loanAmount() / goldValue) * 100;
  });

  maxSafeLoan = computed(() =>
    this.goldWeight() * (REFERENCE_RATES[this.goldPurity()] ?? 12000) * 0.7 // 70% LTV
  );

  maxInterest = computed(() => Math.max(...this.schedule().map(r => r.interest), 1));

  barWidth(interest: number): string {
    const max = this.maxInterest();
    if (!max) return '0%';
    return Math.min(100, Math.round((interest / max) * 100)) + '%';
  }

  formatCurrency(n: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(n);
  }

  // ── Input handlers (signals are the source of truth, not ngModel) ───────────

  onLoanAmountChange(event: CustomEvent): void {
    this.loanAmount.set(Number((event.detail as { value: string }).value) || 0);
  }

  onInterestRateChange(event: CustomEvent): void {
    this.interestRate.set(Number((event.detail as { value: string }).value) || 0);
  }

  onPeriodMonthsChange(event: CustomEvent): void {
    this.periodMonths.set(Math.max(1, Number((event.detail as { value: string }).value) || 1));
  }

  onGoldWeightChange(event: CustomEvent): void {
    this.goldWeight.set(Number((event.detail as { value: string }).value) || 0);
  }

  onGoldPurityChange(event: CustomEvent): void {
    this.goldPurity.set((event.detail as { value: string }).value);
  }

  onInterestTypeChange(event: CustomEvent): void {
    this.interestType.set((event.detail as { value: 'FLAT' | 'REDUCING' }).value);
  }
}