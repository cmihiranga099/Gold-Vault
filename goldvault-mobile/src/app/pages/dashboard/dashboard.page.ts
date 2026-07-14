import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  IonHeader, IonToolbar, IonTitle, IonContent, IonRefresher, IonRefresherContent,
  IonList, IonItem, IonLabel, IonBadge, IonSpinner, IonIcon, IonText, IonSearchbar
} from '@ionic/angular/standalone';
import { RefresherCustomEvent } from '@ionic/angular';
import { addIcons } from 'ionicons';
import { chevronForwardOutline, walletOutline } from 'ionicons/icons';
import { AuthService } from '../../core/auth/auth.service';
import { TicketService } from '../../core/services/ticket.service';
import { PawnTicketResponse, TicketStatus } from '../../core/models/ticket.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    IonHeader, IonToolbar, IonTitle, IonContent, IonRefresher, IonRefresherContent,
    IonList, IonItem, IonLabel, IonBadge, IonSpinner, IonIcon, IonText, IonSearchbar
  ],
  templateUrl: './dashboard.page.html',
  styleUrl: './dashboard.page.scss'
})
export class DashboardPage implements OnInit {
  tickets = signal<PawnTicketResponse[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  searchTerm = signal('');

  constructor(
    private authService: AuthService,
    private ticketService: TicketService,
    private router: Router
  ) {
    addIcons({ chevronForwardOutline, walletOutline });
  }

  ngOnInit(): void {
    this.loadTickets();
  }

  loadTickets(event?: RefresherCustomEvent): void {
    const customerId = this.authService.currentUser()?.customerId;
    if (!customerId) {
      this.errorMessage.set('Not logged in as customer.');
      this.loading.set(false);
      event?.target.complete();
      return;
    }

    if (!event) this.loading.set(true);
    this.ticketService.getMyTickets(customerId).subscribe({
      next: (tickets) => {
        // Active/overdue tickets first, most recently pawned first within each group
        this.tickets.set(
          [...tickets].sort((a, b) => {
            if (a.status !== b.status) {
              const order: Record<TicketStatus, number> = { ACTIVE: 0, EXPIRED: 1, REDEEMED: 2, AUCTIONED: 3 };
              return order[a.status] - order[b.status];
            }
            return new Date(b.pawnDate).getTime() - new Date(a.pawnDate).getTime();
          })
        );
        this.loading.set(false);
        event?.target.complete();
      },
      error: () => {
        this.errorMessage.set('Could not load your tickets.');
        this.loading.set(false);
        event?.target.complete();
      }
    });
  }

  filteredTickets(): PawnTicketResponse[] {
    const term = this.searchTerm().trim().toLowerCase();
    if (!term) return this.tickets();
    return this.tickets().filter(t =>
      t.ticketNumber.toLowerCase().includes(term) ||
      t.shopName.toLowerCase().includes(term));
  }

  onSearchChange(event: CustomEvent): void {
    this.searchTerm.set((event.detail as { value: string }).value ?? '');
  }

  openTicket(ticket: PawnTicketResponse): void {
    this.router.navigateByUrl(`/tickets/${ticket.id}`);
  }

  statusColor(status: TicketStatus): string {
    switch (status) {
      case 'ACTIVE':    return 'success';
      case 'EXPIRED':   return 'warning';
      case 'AUCTIONED': return 'danger';
      default:          return 'medium';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }
}