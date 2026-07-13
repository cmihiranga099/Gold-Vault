import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, IonContent,
  IonSearchbar, IonRefresher, IonRefresherContent, IonList, IonItem, IonLabel,
  IonIcon, IonSpinner, IonText, IonButton
} from '@ionic/angular/standalone';
import { RefresherCustomEvent } from '@ionic/angular';
import { addIcons } from 'ionicons';
import { starOutline, star, callOutline, navigateOutline, storefrontOutline } from 'ionicons/icons';
import { ShopService } from '../../core/services/shop.service';
import { ShopResponse } from '../../core/models/shop.model';

@Component({
  selector: 'app-shop-finder',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, IonContent,
    IonSearchbar, IonRefresher, IonRefresherContent, IonList, IonItem, IonLabel,
    IonIcon, IonSpinner, IonText, IonButton
  ],
  templateUrl: './shop-finder.page.html',
  styleUrl: './shop-finder.page.scss'
})
export class ShopFinderPage implements OnInit {
  shops = signal<ShopResponse[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  searchTerm = signal('');

  constructor(private shopService: ShopService) {
    addIcons({ starOutline, star, callOutline, navigateOutline, storefrontOutline });
  }

  ngOnInit(): void {
    this.load();
  }

  load(event?: RefresherCustomEvent): void {
    if (!event) this.loading.set(true);
    this.shopService.getActiveShops().subscribe({
      next: (shops) => {
        this.shops.set([...shops].sort((a, b) => (b.averageRating ?? 0) - (a.averageRating ?? 0)));
        this.loading.set(false);
        event?.target.complete();
      },
      error: () => {
        this.errorMessage.set('Could not load shops.');
        this.loading.set(false);
        event?.target.complete();
      }
    });
  }

  filteredShops(): ShopResponse[] {
    const term = this.searchTerm().trim().toLowerCase();
    if (!term) return this.shops();
    return this.shops().filter(s =>
      s.name.toLowerCase().includes(term) || (s.address ?? '').toLowerCase().includes(term));
  }

  onSearchChange(event: CustomEvent): void {
    this.searchTerm.set((event.detail as { value: string }).value ?? '');
  }

  starsArray(rating: number | null): number[] {
    const full = Math.round(rating ?? 0);
    return Array.from({ length: 5 }, (_, i) => i + 1 <= full ? 1 : 0);
  }

  callShop(shop: ShopResponse): void {
    if (shop.phone) window.location.href = `tel:${shop.phone}`;
  }

  directionsTo(shop: ShopResponse): void {
    if (shop.latitude != null && shop.longitude != null) {
      window.open(`https://www.google.com/maps/search/?api=1&query=${shop.latitude},${shop.longitude}`, '_system');
    } else if (shop.address) {
      window.open(`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(shop.address)}`, '_system');
    }
  }
}