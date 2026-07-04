import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuctionService } from '../../../core/services/auction.service';
import { AuctionResponse } from '../../../core/models/auction.model';

@Component({
  selector: 'app-auctions-list',
  standalone: true,
  imports: [CommonModule, RouterLink, TagModule, ProgressSpinnerModule, TranslatePipe],
  templateUrl: './auctions.component.html',
  styleUrl:    './auctions.component.scss'
})
export class AuctionsListComponent implements OnInit {
  auctions = signal<AuctionResponse[]>([]);
  loading  = signal(true);
  error    = signal<string | null>(null);

  constructor(private auctionService: AuctionService, private translate: TranslateService) {}

  ngOnInit(): void {
    this.auctionService.getOpenAuctions().subscribe({
      next:  (a) => { this.auctions.set(a); this.loading.set(false); },
      error: ()  => { this.error.set(this.translate.instant('auctionsList.errorLoad')); this.loading.set(false); }
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }

  currentPrice(a: AuctionResponse): number {
    return a.currentBid ?? a.startingPrice;
  }

  timeLeft(endsAt: string): string {
    const diff = new Date(endsAt).getTime() - Date.now();
    if (diff <= 0) return this.translate.instant('auctionsList.ended');
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    return days > 0
      ? this.translate.instant('auctionsList.daysHoursLeft', { days, hours })
      : this.translate.instant('auctionsList.hoursLeft', { hours });
  }
}