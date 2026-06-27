import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AuthService } from '../../../core/auth/auth.service';
import { MarketplaceService } from '../../../core/services/marketplace.service';
import { GoldListingResponse, GoldOfferResponse, ListingStatus, OfferStatus } from '../../../core/models/marketplace.model';

@Component({
  selector: 'app-shop-marketplace',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    ButtonModule, InputTextModule, InputNumberModule, MessageModule, TagModule, DialogModule,
    TopnavComponent
  ],
  templateUrl: './marketplace.component.html',
  styleUrl: './marketplace.component.scss'
})
export class ShopMarketplaceComponent implements OnInit {
  listings = signal<GoldListingResponse[]>([]);
  myOffers = signal<GoldOfferResponse[]>([]);
  loading = signal(true);
errorMessage = signal<string | null>(null);

  selectedListing = signal<GoldListingResponse | null>(null);
  showOfferDialog = false;
  offerLoading = signal(false);
  offerError = signal<string | null>(null);

  private shopId: number | null = null;

  form: ReturnType<FormBuilder['group']>;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private marketplaceService: MarketplaceService
  ) {
    this.form = this.fb.group({
      offerPrice: [null as number | null, [Validators.required, Validators.min(0.01)]],
      message: ['']
    });
  }

  ngOnInit(): void {
    this.shopId = this.authService.currentUser()?.shopId ?? null;
    this.loadAll();
  }

  loadAll(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
  
    this.marketplaceService.openListings().subscribe({
      next: (listings) => {
        this.listings.set(listings);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not load open listings. Please try again.');
        this.loading.set(false);
      }
    });
  
    if (this.shopId) {
      this.marketplaceService.getShopOffers(this.shopId).subscribe({
        next: (offers) => this.myOffers.set(offers),
        error: () => this.myOffers.set([])
      });
    }
  }

  /** Returns this shop's offer status on a listing, if any */
  myOfferStatus(listingId: number): OfferStatus | null {
    const offer = this.myOffers().find(o => o.listingId === listingId);
    return offer ? offer.status : null;
  }

  openOfferDialog(listing: GoldListingResponse): void {
    this.selectedListing.set(listing);
    this.offerError.set(null);
    this.form.reset();
    this.showOfferDialog = true;
  }

  onSubmitOffer(): void {
    const listing = this.selectedListing();
    if (this.form.invalid || !this.shopId || !listing) {
      this.form.markAllAsTouched();
      return;
    }

    this.offerLoading.set(true);
    this.offerError.set(null);

    const raw = this.form.getRawValue();

    this.marketplaceService.submitOffer(this.shopId, {
      listingId: listing.id,
      offerPrice: raw.offerPrice,
      message: raw.message || undefined
    }).subscribe({
      next: () => {
        this.offerLoading.set(false);
        this.showOfferDialog = false;
        this.loadAll();
      },
      error: (err) => {
        this.offerLoading.set(false);
        this.offerError.set(err?.error?.message || 'Could not submit offer.');
      }
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }
}