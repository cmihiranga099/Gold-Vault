import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../core/auth/auth.service';
import { TicketService } from '../../../core/services/ticket.service';
import { PawnTicketResponse, TicketStatus } from '../../../core/models/ticket.model';

type FilterStatus = 'ALL' | TicketStatus;

@Component({
  selector: 'app-shop-tickets',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, TagModule, InputTextModule, ProgressSpinnerModule, TranslatePipe],
  templateUrl: './tickets.component.html',
  styleUrl: './tickets.component.scss'
})
export class ShopTicketsComponent implements OnInit {
  tickets = signal<PawnTicketResponse[]>([]);
  loading = signal(true);
  error   = signal<string | null>(null);

  search = '';
  filter = signal<FilterStatus>('ALL');

  filterOptions: FilterStatus[] = ['ALL', 'ACTIVE', 'EXPIRED', 'REDEEMED', 'AUCTIONED'];

  constructor(
    private authService: AuthService,
    private ticketService: TicketService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    const shopId = this.authService.currentUser()?.shopId;
    if (!shopId) { this.error.set(this.translate.instant('shopTickets.errNoShop')); this.loading.set(false); return; }

    this.ticketService.getShopTickets(shopId).subscribe({
      next:  (tickets) => { this.tickets.set(tickets); this.loading.set(false); },
      error: ()         => { this.error.set(this.translate.instant('shopTickets.errLoad')); this.loading.set(false); }
    });
  }

  filtered(): PawnTicketResponse[] {
    let list = this.tickets();

    if (this.filter() !== 'ALL') {
      list = list.filter(t => t.status === this.filter());
    }

    const q = this.search.trim().toLowerCase();
    if (q) {
      list = list.filter(t =>
        t.ticketNumber.toLowerCase().includes(q) ||
        t.customerName.toLowerCase().includes(q) ||
        t.customerNic.toLowerCase().includes(q)
      );
    }

    return list;
  }

  setFilter(f: FilterStatus): void {
    this.filter.set(f);
  }

  filterLabel(f: FilterStatus): string {
    const map: Record<FilterStatus, string> = {
      ALL: 'shopTickets.filterAll',
      ACTIVE: 'shopTickets.filterActive',
      EXPIRED: 'shopTickets.filterExpired',
      REDEEMED: 'shopTickets.filterRedeemed',
      AUCTIONED: 'shopTickets.filterAuctioned'
    };
    return this.translate.instant(map[f]);
  }

  statusSeverity(status: string): 'success' | 'warn' | 'danger' | 'secondary' {
    switch (status) {
      case 'ACTIVE':    return 'success';
      case 'EXPIRED':   return 'warn';
      case 'AUCTIONED': return 'danger';
      default:          return 'secondary';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }
}