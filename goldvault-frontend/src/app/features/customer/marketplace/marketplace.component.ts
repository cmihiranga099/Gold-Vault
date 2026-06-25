import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { MessageModule } from 'primeng/message';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AuthService } from '../../../core/auth/auth.service';
import { MarketplaceService } from '../../../core/services/marketplace.service';
import { GoldListingResponse, GoldPurity, GoldRateResponse, ListingStatus, OfferStatus } from '../../../core/models/marketplace.model';

const PURITIES: GoldPurity[] = ['K24', 'K22', 'K21', 'K18', 'P916', 'P750', 'OTHER'];

@Component({
  selector: 'app-customer-marketplace',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    ButtonModule, InputTextModule, InputNumberModule, SelectModule, MessageModule, TagModule, DialogModule,
    TopnavComponent
  ],
  templateUrl: './marketplace.component.html',
  styleUrl: './marketplace.component.scss'
})
export class CustomerMarketplaceComponent implements OnInit {
  rates = signal<GoldRateResponse[]>([]);
  listings = signal<GoldListingResponse[]>([]);
  loading = signal(true);

  showCreateDialog = false;
  createLoading = signal(false);
  createError = signal<string | null>(null);

  purities = PURITIES;
  private customerId: number | null = null;

  form: ReturnType<FormBuilder['group']>;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private marketplaceService: MarketplaceService
  ) {
    this.form = this.fb.group({
      description: [''],
      weightGrams: [null as number | null, [Validators.required, Validators.min(0.001)]],
      purity: ['K22', Validators.required],
      askingPrice: [null as number | null]
    });
  }

  ngOnInit(): void {
    this.customerId = this.authService.currentUser()?.customerId ?? null;
    this.loadAll();
  }

  private loadAll(): void {
    this.loading.set(true);

    this.marketplaceService.compareRates().subscribe({
      next: (rates) => this.rates.set(rates),
      error: () => this.rates.set([])
    });

    if (this.customerId) {
      this.marketplaceService.getMyListings(this.customerId).subscribe({
        next: (listings) => {
          this.listings.set(listings);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
    } else {
      this.loading.set(false);
    }
  }

  openCreateDialog(): void {
    this.createError.set(null);
    this.showCreateDialog = true;
  }

  onCreateListing(): void {
    if (this.form.invalid || !this.customerId) {
      this.form.markAllAsTouched();
      return;
    }

    this.createLoading.set(true);
    this.createError.set(null);

    const raw = this.form.getRawValue();

    this.marketplaceService.createListing(this.customerId, {
      description: raw.description || undefined,
      weightGrams: raw.weightGrams,
      purity: raw.purity,
      askingPrice: raw.askingPrice ?? undefined
    }).subscribe({
      next: () => {
        this.createLoading.set(false);
        this.showCreateDialog = false;
        this.form.reset({ purity: 'K22' });
        this.loadAll();
      },
      error: (err) => {
        this.createLoading.set(false);
        this.createError.set(err?.error?.message || 'Could not create listing.');
      }
    });
  }

  listingSeverity(status: ListingStatus): 'success' | 'info' | 'warn' | 'secondary' {
    switch (status) {
      case 'OPEN': return 'success';
      case 'UNDER_REVIEW': return 'info';
      case 'SOLD': return 'secondary';
      default: return 'warn';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }
}