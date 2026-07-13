import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import {
  IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, IonContent,
  IonSpinner, IonText, IonBadge, IonButton, IonList, IonItem, IonLabel, IonIcon
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { chatbubbleOutline, closeCircleOutline } from 'ionicons/icons';
import { AuthService } from '../../core/auth/auth.service';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { GoldListingResponse, OfferStatus } from '../../core/models/marketplace.model';

@Component({
  selector: 'app-listing-detail',
  standalone: true,
  imports: [
    CommonModule,
    IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, IonContent,
    IonSpinner, IonText, IonBadge, IonButton, IonList, IonItem, IonLabel, IonIcon
  ],
  templateUrl: './listing-detail.page.html',
  styleUrl: './listing-detail.page.scss'
})
export class ListingDetailPage implements OnInit {
  listing = signal<GoldListingResponse | null>(null);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  actionLoading = signal<number | null>(null);
  withdrawing = signal(false);

  private listingId!: number;
  private customerId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private marketplaceService: MarketplaceService
  ) {
    addIcons({ chatbubbleOutline, closeCircleOutline });
  }

  ngOnInit(): void {
    this.listingId = Number(this.route.snapshot.paramMap.get('id'));
    this.customerId = this.authService.currentUser()?.customerId ?? null;

    if (!this.listingId) {
      this.errorMessage.set('Invalid listing.');
      this.loading.set(false);
      return;
    }
    this.load();
  }

  private load(): void {
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

  canWithdraw(): boolean {
    return this.listing()?.status === 'OPEN' || this.listing()?.status === 'UNDER_REVIEW';
  }

  withdrawListing(): void {
    if (!this.customerId) return;
    this.withdrawing.set(true);
    this.marketplaceService.withdrawListing(this.listingId, this.customerId).subscribe({
      next: (listing) => {
        this.withdrawing.set(false);
        this.listing.set(listing);
      },
      error: () => this.withdrawing.set(false)
    });
  }

  acceptOffer(offerId: number): void {
    if (!this.customerId) return;
    this.actionLoading.set(offerId);
    this.marketplaceService.acceptOffer(offerId, this.customerId).subscribe({
      next: () => { this.actionLoading.set(null); this.load(); },
      error: () => this.actionLoading.set(null)
    });
  }

  rejectOffer(offerId: number): void {
    if (!this.customerId) return;
    this.actionLoading.set(offerId);
    this.marketplaceService.rejectOffer(offerId, this.customerId).subscribe({
      next: () => { this.actionLoading.set(null); this.load(); },
      error: () => this.actionLoading.set(null)
    });
  }

  offerColor(status: OfferStatus): string {
    switch (status) {
      case 'ACCEPTED': return 'success';
      case 'REJECTED': return 'danger';
      case 'PENDING': return 'warning';
      default: return 'medium';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }
}