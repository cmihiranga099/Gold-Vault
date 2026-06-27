import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ButtonModule } from 'primeng/button';
import { TranslatePipe } from '@ngx-translate/core';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { TicketService } from '../../../core/services/ticket.service';
import { PaymentService } from '../../../core/services/payment.service';
import { PawnTicketResponse } from '../../../core/models/ticket.model';
import { PaymentResponse } from '../../../core/models/payment.model';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, TagModule, ProgressSpinnerModule, ButtonModule, TranslatePipe, TopnavComponent],
  templateUrl: './ticket-detail.component.html',
  styleUrl: './ticket-detail.component.scss'
})
export class TicketDetailComponent implements OnInit {
  ticket = signal<PawnTicketResponse | null>(null);
  payments = signal<PaymentResponse[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);

  constructor(
    private route: ActivatedRoute,
    private ticketService: TicketService,
    private paymentService: PaymentService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMessage.set('Invalid ticket.');
      this.loading.set(false);
      return;
    }

    this.ticketService.getTicketDetail(id).subscribe({
      next: (ticket) => {
        this.ticket.set(ticket);
        this.loading.set(false);
        this.loadPayments(id);
      },
      error: () => {
        this.errorMessage.set('Could not load this ticket.');
        this.loading.set(false);
      }
    });
  }

  private loadPayments(ticketId: number): void {
    this.paymentService.getCustomerPaymentHistory(ticketId).subscribe({
      next: (payments) => this.payments.set(payments),
      error: () => this.payments.set([])
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