import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, IonContent,
  IonRefresher, IonRefresherContent, IonList, IonItem, IonLabel, IonBadge,
  IonSpinner, IonIcon, IonText
} from '@ionic/angular/standalone';
import { RefresherCustomEvent } from '@ionic/angular';
import { addIcons } from 'ionicons';
import { addOutline, locationOutline, diamondOutline, chevronForwardOutline } from 'ionicons/icons';
import { AuthService } from '../../core/auth/auth.service';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { GoldListingResponse, GoldRateResponse, ListingStatus } from '../../core/models/marketplace.model';

@Component({
  selector: 'app-marketplace',
  standalone: true,
  imports: [
    CommonModule,
    IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, IonContent,
    IonRefresher, IonRefresherContent, IonList, IonItem, IonLabel, IonBadge,
    IonSpinner, IonIcon, IonText
  ],
  templateUrl: './marketplace.page.html',
  styleUrl: './marketplace.page.scss'
})
export class MarketplacePage implements OnInit {
  rates = signal<GoldRateResponse[]>([]);
  listings = signal<GoldListingResponse[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);

  private customerId: number | null = null;

  constructor(
    private authService: AuthService,
    private marketplaceService: MarketplaceService,
    private router: Router
  ) {
    addIcons({ addOutline, locationOutline, diamondOutline, chevronForwardOutline });
  }

  ngOnInit(): void {
    this.customerId = this.authService.currentUser()?.customerId ?? null;
    this.load();
  }

  load(event?: RefresherCustomEvent): void {
    if (!event) this.loading.set(true);
    this.errorMessage.set(null);

    this.marketplaceService.compareRates().subscribe({
      next: (rates) => this.rates.set(rates),
      error: () => this.rates.set([])
    });

    if (this.customerId) {
      this.marketplaceService.getMyListings(this.customerId).subscribe({
        next: (listings) => {
          this.listings.set(listings);
          this.loading.set(false);
          event?.target.complete();
        },
        error: () => {
          this.errorMessage.set('Could not load your listings.');
          this.loading.set(false);
          event?.target.complete();
        }
      });
    } else {
      this.loading.set(false);
      event?.target.complete();
    }
  }

  bestRateFor(purity: string): GoldRateResponse | null {
    const matching = this.rates().filter(r => r.purity === purity);
    if (matching.length === 0) return null;
    return matching.reduce((best, r) => r.ratePerGram > best.ratePerGram ? r : best);
  }

  goToCreateListing(): void {
    this.router.navigateByUrl('/create-listing');
  }

  goToListing(listing: GoldListingResponse): void {
    this.router.navigateByUrl(`/listings/${listing.id}`);
  }

  goToShopFinder(): void {
    this.router.navigateByUrl('/shop-finder');
  }

  listingColor(status: ListingStatus): string {
    switch (status) {
      case 'OPEN': return 'success';
      case 'UNDER_REVIEW': return 'primary';
      case 'SOLD': return 'medium';
      default: return 'warning';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }
}