import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslatePipe } from '@ngx-translate/core';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { CustomerService } from '../../../core/services/customer.service';
import { TicketService } from '../../../core/services/ticket.service';
import { CustomerResponse } from '../../../core/models/customer.model';
import { PawnTicketResponse } from '../../../core/models/ticket.model';

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, TagModule, ButtonModule, ProgressSpinnerModule, TranslatePipe, TopnavComponent],
  templateUrl: './customer-detail.component.html',
  styleUrl: './customer-detail.component.scss'
})
export class CustomerDetailComponent implements OnInit {
  customer = signal<CustomerResponse | null>(null);
  tickets = signal<PawnTicketResponse[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);

  constructor(
    private route: ActivatedRoute,
    private customerService: CustomerService,
    private ticketService: TicketService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMessage.set('Invalid customer.');
      this.loading.set(false);
      return;
    }

    this.customerService.getById(id).subscribe({
      next: (customer) => {
        this.customer.set(customer);
        this.loading.set(false);
        this.loadTickets(id);
      },
      error: () => {
        this.errorMessage.set('Could not load this customer.');
        this.loading.set(false);
      }
    });
  }

  private loadTickets(customerId: number): void {
    this.ticketService.getShopTickets(this.customer()!.shopId).subscribe({
      next: (allTickets) => {
        this.tickets.set(allTickets.filter(t => t.customerId === customerId));
      },
      error: () => this.tickets.set([])
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