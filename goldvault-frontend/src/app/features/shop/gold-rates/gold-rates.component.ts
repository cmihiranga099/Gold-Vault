import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { MessageModule } from 'primeng/message';
import { TagModule } from 'primeng/tag';
import { TranslatePipe } from '@ngx-translate/core';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AuthService } from '../../../core/auth/auth.service';
import { MarketplaceService } from '../../../core/services/marketplace.service';
import { GoldPurity, GoldRateResponse } from '../../../core/models/marketplace.model';

const PURITIES: GoldPurity[] = ['K24', 'K22', 'K21', 'K18', 'P916', 'P750', 'OTHER'];

@Component({
  selector: 'app-shop-gold-rates',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    ButtonModule, InputNumberModule, SelectModule, MessageModule, TagModule,
    TranslatePipe,
    TopnavComponent
  ],
  templateUrl: './gold-rates.component.html',
  styleUrl: './gold-rates.component.scss'
})
export class GoldRatesComponent implements OnInit {
  rates = signal<GoldRateResponse[]>([]);
  loading = signal(true);
  publishing = signal(false);
  errorMessage = signal<string | null>(null);
  publishError = signal<string | null>(null);

  purities = PURITIES;
  private shopId: number | null = null;

  form: ReturnType<FormBuilder['group']>;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private marketplaceService: MarketplaceService
  ) {
    this.form = this.fb.group({
      purity: ['K22', Validators.required],
      ratePerGram: [null as number | null, [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    this.shopId = this.authService.currentUser()?.shopId ?? null;

    if (!this.shopId) {
      this.errorMessage.set('No shop linked to this account.');
      this.loading.set(false);
      return;
    }

    this.loadRates();
  }

   loadRates(): void {
    if (!this.shopId) return;
    this.loading.set(true);
    this.marketplaceService.getShopRates(this.shopId).subscribe({
      next: (rates) => {
        this.rates.set(rates);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not load rates.');
        this.loading.set(false);
      }
    });
  }

  onPublish(): void {
    if (this.form.invalid || !this.shopId) {
      this.form.markAllAsTouched();
      return;
    }

    this.publishing.set(true);
    this.publishError.set(null);

    const raw = this.form.getRawValue();

    this.marketplaceService.publishRate(this.shopId, {
      purity: raw.purity,
      ratePerGram: raw.ratePerGram
    }).subscribe({
      next: () => {
        this.publishing.set(false);
        this.form.patchValue({ ratePerGram: null });
        this.loadRates();
      },
      error: (err) => {
        this.publishing.set(false);
        this.publishError.set(err?.error?.message || 'Could not publish rate.');
      }
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }
}