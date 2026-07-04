import { Component, OnInit, AfterViewInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslatePipe } from '@ngx-translate/core';
import * as L from 'leaflet';
import { ShopService } from '../../../core/services/shop.service';
import { ShopResponse } from '../../../core/models/shop.model';

// Fix default marker icon path issue with Angular/Webpack bundling
const goldIcon = L.icon({
  iconUrl: 'data:image/svg+xml;base64,' + btoa(`
    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="42" viewBox="0 0 32 42">
      <path d="M16 0C7.2 0 0 7.2 0 16c0 11 16 26 16 26s16-15 16-26C32 7.2 24.8 0 16 0z" fill="#C9A14A"/>
      <circle cx="16" cy="16" r="7" fill="#1A1A2E"/>
    </svg>
  `),
  iconSize: [32, 42],
  iconAnchor: [16, 42],
  popupAnchor: [0, -38]
});

const userIcon = L.icon({
  iconUrl: 'data:image/svg+xml;base64,' + btoa(`
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
      <circle cx="12" cy="12" r="8" fill="#3B8BD4" stroke="#fff" stroke-width="3"/>
    </svg>
  `),
  iconSize: [24, 24],
  iconAnchor: [12, 12]
});

@Component({
  selector: 'app-shop-finder',
  standalone: true,
  imports: [CommonModule, RouterLink, ProgressSpinnerModule, TranslatePipe],
  templateUrl: './shop-finder.component.html',
  styleUrl: './shop-finder.component.scss'
})
export class ShopFinderComponent implements OnInit, AfterViewInit {
  protected Math = Math;

  shops    = signal<ShopResponse[]>([]);
  loading  = signal(true);
  error    = signal<string | null>(null);
  selected = signal<ShopResponse | null>(null);
  userLocation: { lat: number; lng: number } | null = null;

  private map!: L.Map;
  private markers: L.Marker[] = [];

  // Default center: Colombo, Sri Lanka
  private readonly DEFAULT_CENTER: [number, number] = [6.9271, 79.8612];

  constructor(private shopService: ShopService) {}

  ngOnInit(): void {
    this.shopService.getActiveShops().subscribe({
      next: (shops) => {
        // Only show shops that have a pinned location
        this.shops.set(shops.filter(s => s.latitude != null && s.longitude != null));
        this.loading.set(false);
        if (this.map) this.renderMarkers();
      },
      error: () => {
        this.error.set('Could not load shops. Please try again.');
        this.loading.set(false);
      }
    });
  }

  ngAfterViewInit(): void {
    this.initMap();
    this.tryLocateUser();
  }

  private initMap(): void {
    this.map = L.map('shop-map', {
      center: this.DEFAULT_CENTER,
      zoom: 8,
      zoomControl: true
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      maxZoom: 19
    }).addTo(this.map);

    if (this.shops().length > 0) this.renderMarkers();
  }

  private renderMarkers(): void {
    // Clear old markers first
    this.markers.forEach(m => m.remove());
    this.markers = [];

    const bounds: L.LatLngExpression[] = [];

    this.shops().forEach(shop => {
      const lat = Number(shop.latitude);
      const lng = Number(shop.longitude);
      bounds.push([lat, lng]);

      const marker = L.marker([lat, lng], { icon: goldIcon }).addTo(this.map);

      const ratingHtml = shop.averageRating && shop.totalReviews
        ? `<div style="margin-top:4px;font-size:12px;color:#C9A14A;">
             ${'★'.repeat(Math.round(shop.averageRating))}${'☆'.repeat(5 - Math.round(shop.averageRating))}
             <span style="color:#888;"> (${shop.totalReviews})</span>
           </div>`
        : '';

        marker.bindPopup(`
          <div style="font-family:inherit;min-width:200px;">
            <strong style="font-size:14px;">${shop.name}</strong>
            ${ratingHtml}
            <div style="font-size:12px;color:#666;margin-top:6px;">${shop.address ?? ''}</div>
            ${shop.phone ? `<div style="font-size:12px;color:#666;margin-top:2px;">📞 ${shop.phone}</div>` : ''}
            <a href="/shops/${shop.id}"
               style="display:block;margin-top:10px;background:#C9A14A;color:#fff;text-align:center;
                      padding:6px;border-radius:6px;text-decoration:none;font-size:12px;font-weight:600;">
              View full profile →
            </a>
          </div>
        `);

      marker.on('click', () => this.selected.set(shop));
      this.markers.push(marker);
    });

    if (bounds.length > 0) {
      this.map.fitBounds(L.latLngBounds(bounds), { padding: [40, 40], maxZoom: 13 });
    }
  }

  private tryLocateUser(): void {
    if (!navigator.geolocation) return;

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.userLocation = { lat: pos.coords.latitude, lng: pos.coords.longitude };
        L.marker([pos.coords.latitude, pos.coords.longitude], { icon: userIcon })
          .addTo(this.map)
          .bindPopup('You are here');
      },
      () => { /* permission denied — silently ignore, map still works */ },
      { timeout: 5000 }
    );
  }

  focusShop(shop: ShopResponse): void {
    this.selected.set(shop);
    if (shop.latitude != null && shop.longitude != null) {
      this.map.setView([Number(shop.latitude), Number(shop.longitude)], 15);
      const marker = this.markers.find((_, i) => this.shops()[i].id === shop.id);
      marker?.openPopup();
    }
  }

  distanceFromUser(shop: ShopResponse): string | null {
    if (!this.userLocation || shop.latitude == null || shop.longitude == null) return null;
    const d = this.haversine(
      this.userLocation.lat, this.userLocation.lng,
      Number(shop.latitude), Number(shop.longitude)
    );
    return d < 1 ? `${Math.round(d * 1000)} m` : `${d.toFixed(1)} km`;
  }

  private haversine(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const R = 6371;
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) ** 2 +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLon / 2) ** 2;
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  sortedShops(): ShopResponse[] {
    if (!this.userLocation) return this.shops();
    return [...this.shops()].sort((a, b) => {
      const da = this.haversine(this.userLocation!.lat, this.userLocation!.lng, Number(a.latitude), Number(a.longitude));
      const db = this.haversine(this.userLocation!.lat, this.userLocation!.lng, Number(b.latitude), Number(b.longitude));
      return da - db;
    });
  }
}