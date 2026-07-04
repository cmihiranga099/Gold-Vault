import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

interface AmortisationRow {
  month:          number;
  openingBalance: number;
  interest:       number;
  payment:        number;
  closingBalance: number;
}

@Component({
  selector: 'app-interest-calculator',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslatePipe],
  templateUrl: './calculator.component.html',
  styleUrl:    './calculator.component.scss'
})
export class InterestCalculatorComponent {

  // ── Inputs ───────────────────────────────────────────────────────────────────
  loanAmount    = signal(50000);
  interestRate  = signal(2.5);      // % per month
  periodMonths  = signal(6);
  interestType  = signal<'FLAT' | 'REDUCING'>('FLAT');
  goldWeight    = signal(10);       // grams
  goldPurity    = signal('K22');

  purities = ['K24', 'K22', 'K21', 'K18', 'P916', 'P750', 'OTHER'];

  // ── Derived calculations ──────────────────────────────────────────────────────

  schedule = computed<AmortisationRow[]>(() => {
    const principal = this.loanAmount();
    const rate      = this.interestRate() / 100;
    const months    = this.periodMonths();
    const type      = this.interestType();

    const rows: AmortisationRow[] = [];

    if (type === 'FLAT') {
      const monthlyInterest = principal * rate;
      const monthlyPayment  = monthlyInterest; // flat = interest only each month, principal at end

      for (let m = 1; m <= months; m++) {
        const isLast = m === months;
        rows.push({
          month:          m,
          openingBalance: principal,
          interest:       monthlyInterest,
          payment:        isLast ? monthlyInterest + principal : monthlyPayment,
          closingBalance: isLast ? 0 : principal
        });
      }
    } else {
      // Reducing balance
      let balance = principal;
      for (let m = 1; m <= months; m++) {
        const interest = balance * rate;
        const principal_payment = principal / months;
        const payment  = interest + principal_payment;
        const closing  = Math.max(0, balance - principal_payment);
        rows.push({
          month:          m,
          openingBalance: balance,
          interest:       interest,
          payment:        payment,
          closingBalance: closing
        });
        balance = closing;
      }
    }

    return rows;
  });

  totalInterest = computed(() =>
    this.schedule().reduce((sum, r) => sum + r.interest, 0)
  );

  totalPayable = computed(() =>
    this.loanAmount() + this.totalInterest()
  );

  monthlyPayment = computed(() =>
    this.schedule()[0]?.payment ?? 0
  );

  effectiveCostPct = computed(() => {
    const total = this.totalInterest();
    const principal = this.loanAmount();
    if (!principal) return 0;
    return (total / principal) * 100;
  });

  loanToValueRatio = computed(() => {
    const rates: Record<string, number> = {
      K24: 18500, K22: 17000, K21: 16200, K18: 13900,
      P916: 16950, P750: 13875, OTHER: 12000
    };
    const ratePerGram = rates[this.goldPurity()] ?? 12000;
    const goldValue = this.goldWeight() * ratePerGram;
    if (!goldValue) return 0;
    return (this.loanAmount() / goldValue) * 100;
  });

  maxSafeLoan = computed(() => {
    const rates: Record<string, number> = {
      K24: 18500, K22: 17000, K21: 16200, K18: 13900,
      P916: 16950, P750: 13875, OTHER: 12000
    };
    return this.goldWeight() * (rates[this.goldPurity()] ?? 12000) * 0.7; // 70% LTV
  });

  barWidth(interest: number, max: number): string {
    if (!max) return '0%';
    return Math.min(100, Math.round((interest / max) * 100)) + '%';
  }

  maxInterest = computed(() =>
    Math.max(...this.schedule().map(r => r.interest), 1)
  );

  formatCurrency(n: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(n);
  }
}