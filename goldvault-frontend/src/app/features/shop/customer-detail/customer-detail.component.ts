import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { CustomerService } from '../../../core/services/customer.service';
import { TicketService } from '../../../core/services/ticket.service';
import { ReviewService } from '../../../core/services/review.service';
import { AuthService } from '../../../core/auth/auth.service';
import { CustomerResponse } from '../../../core/models/customer.model';
import { PawnTicketResponse } from '../../../core/models/ticket.model';
import { ReviewResponse, ShopRatingResponse } from '../../../core/models/review.model';

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [
    CommonModule, RouterLink,
    TagModule, ButtonModule, ProgressSpinnerModule,
    TranslatePipe
  ],
  templateUrl: './customer-detail.component.html',
  styleUrl:    './customer-detail.component.scss'
})
export class CustomerDetailComponent implements OnInit {
  customer     = signal<CustomerResponse | null>(null);
  tickets      = signal<PawnTicketResponse[]>([]);
  reviews      = signal<ReviewResponse[]>([]);
  shopRating   = signal<ShopRatingResponse | null>(null);
  loading      = signal(true);
  errorMessage = signal<string | null>(null);
  activeTab    = signal<'tickets' | 'reviews'>('tickets');
  protected Math = Math;

  constructor(
    private route:         ActivatedRoute,
    private customerService: CustomerService,
    private ticketService:   TicketService,
    private reviewService:   ReviewService,
    private authService:     AuthService,
    private translate:       TranslateService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) { this.errorMessage.set(this.translate.instant('customerDetailPage.errInvalid')); this.loading.set(false); return; }

    this.customerService.getById(id).subscribe({
      next: (customer) => {
        this.customer.set(customer);
        this.loading.set(false);
        this.loadTickets(id);
        this.loadShopReviews(customer.shopId);
      },
      error: () => { this.errorMessage.set(this.translate.instant('customerDetailPage.errLoad')); this.loading.set(false); }
    });
  }

  private loadTickets(customerId: number): void {
    this.ticketService.getShopTickets(this.customer()!.shopId).subscribe({
      next:  (all) => this.tickets.set(all.filter(t => t.customerId === customerId)),
      error: ()    => this.tickets.set([])
    });
  }

  private loadShopReviews(shopId: number): void {
    this.reviewService.getMyShopReviews(shopId).subscribe({
      next:  (reviews) => this.reviews.set(reviews),
      error: ()        => this.reviews.set([])
    });

    this.reviewService.getShopRating(shopId).subscribe({
      next:  (rating) => this.shopRating.set(rating),
      error: ()       => {}
    });
  }

  starsArray(n: number): number[] {
    return Array.from({ length: 5 }, (_, i) => i + 1);
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