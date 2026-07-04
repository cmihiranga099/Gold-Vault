import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../../core/auth/auth.service';
import { MarketplaceService } from '../../../core/services/marketplace.service';
import { GoldListingResponse, OfferStatus } from '../../../core/models/marketplace.model';

@Component({
  selector: 'app-listing-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, TagModule, ButtonModule, ProgressSpinnerModule, TranslatePipe],
  templateUrl: './listing-detail.component.html',
  styleUrl: './listing-detail.component.scss'
})
export class ListingDetailComponent implements OnInit {
  listing = signal<GoldListingResponse | null>(null);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  actionLoading = signal<number | null>(null);

  private listingId!: number;
  private customerId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private marketplaceService: MarketplaceService
  ) {}

  ngOnInit(): void {
    this.listingId = Number(this.route.snapshot.paramMap.get('id'));
    this.customerId = this.authService.currentUser()?.customerId ?? null;

    if (!this.listingId) {
      this.errorMessage.set('Invalid listing.');
      this.loading.set(false);
      return;
    }

    this.loadListing();
  }

  private loadListing(): void {
    this.loading.set(true);
    this.marketplaceService.getListingDetail(this.listingId).subscribe({
      next: (listing) => {
        this.listing.set(listing);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not load this listing.');
        this.loading.set(false);
      }
    });
  }

  acceptOffer(offerId: number): void {
    if (!this.customerId) return;
    this.actionLoading.set(offerId);

    this.marketplaceService.acceptOffer(offerId, this.customerId).subscribe({
      next: () => {
        this.actionLoading.set(null);
        this.loadListing();
      },
      error: () => this.actionLoading.set(null)
    });
  }

  rejectOffer(offerId: number): void {
    if (!this.customerId) return;
    this.actionLoading.set(offerId);

    this.marketplaceService.rejectOffer(offerId, this.customerId).subscribe({
      next: () => {
        this.actionLoading.set(null);
        this.loadListing();
      },
      error: () => this.actionLoading.set(null)
    });
  }

  offerSeverity(status: OfferStatus): 'success' | 'danger' | 'warn' | 'secondary' {
    switch (status) {
      case 'ACCEPTED': return 'success';
      case 'REJECTED': return 'danger';
      case 'PENDING': return 'warn';
      default: return 'secondary';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }
}