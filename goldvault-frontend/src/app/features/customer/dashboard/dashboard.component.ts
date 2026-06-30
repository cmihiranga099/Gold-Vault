import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslatePipe } from '@ngx-translate/core';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AuthService } from '../../../core/auth/auth.service';
import { TicketService } from '../../../core/services/ticket.service';
import { PawnTicketResponse } from '../../../core/models/ticket.model';
import { LoyaltyService } from '../../../core/services/loyalty.service';
import { LoyaltySummaryResponse } from '../../../core/models/loyalty.model';

@Component({
  selector: 'app-customer-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, TagModule, ButtonModule, ProgressSpinnerModule, TranslatePipe, TopnavComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  tickets = signal<PawnTicketResponse[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  loyalty = signal<LoyaltySummaryResponse | null>(null);

  activeTickets = computed(() => this.tickets().filter(t => t.status === 'ACTIVE'));

  totalOutstanding = computed(() =>
    this.activeTickets().reduce((sum, t) => sum + t.outstandingBalance, 0)
  );

  nextDueTicket = computed(() => {
    const active = this.activeTickets();
    if (active.length === 0) return null;
    return active.reduce((earliest, t) =>
      new Date(t.expiryDate) < new Date(earliest.expiryDate) ? t : earliest
    );
  });

  constructor(
    private authService: AuthService,
    private ticketService: TicketService,
    private loyaltyService: LoyaltyService
  ) {}

  ngOnInit(): void {
    const customerId = this.authService.currentUser()?.customerId;

    if (!customerId) {
      this.errorMessage.set('No customer profile linked to this account.');
      this.loading.set(false);
      return;
    }

    this.ticketService.getMyTickets(customerId).subscribe({
      next: (tickets) => {
        this.tickets.set(tickets);
        this.loading.set(false);
      },
      
      error: () => {
        this.errorMessage.set('Could not load your tickets. Please try again.');
        this.loading.set(false);
      }
      
    });

    this.loyaltyService.getSummary(customerId).subscribe({
      next: (summary) => this.loyalty.set(summary),
      error: () => {}
    });
  }

  statusSeverity(status: string): 'success' | 'warn' | 'danger' | 'secondary' {
    switch (status) {
      case 'ACTIVE': return 'success';
      case 'EXPIRED': return 'warn';
      case 'AUCTIONED': return 'danger';
      default: return 'secondary';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }
}