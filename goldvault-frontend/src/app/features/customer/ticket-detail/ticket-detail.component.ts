import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ButtonModule } from 'primeng/button';
import { TextareaModule } from 'primeng/textarea';
import { MessageModule } from 'primeng/message';
import { TranslatePipe } from '@ngx-translate/core';
import { TicketService } from '../../../core/services/ticket.service';
import { PaymentService } from '../../../core/services/payment.service';
import { ReviewService } from '../../../core/services/review.service';
import { AuthService } from '../../../core/auth/auth.service';
import { PawnTicketResponse } from '../../../core/models/ticket.model';
import { PaymentResponse } from '../../../core/models/payment.model';
import { ReviewResponse } from '../../../core/models/review.model';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [
    CommonModule, RouterLink, ReactiveFormsModule,
    TagModule, ProgressSpinnerModule, ButtonModule,
    TextareaModule, MessageModule,
    TranslatePipe
  ],
  templateUrl: './ticket-detail.component.html',
  styleUrl: './ticket-detail.component.scss'
})
export class CustomerTicketDetailComponent implements OnInit {
  ticket       = signal<PawnTicketResponse | null>(null);
  payments     = signal<PaymentResponse[]>([]);
  loading      = signal(true);
  errorMessage = signal<string | null>(null);

  // ── Review state ─────────────────────────────────────────────────────────────
  existingReview   = signal<ReviewResponse | null>(null);
  alreadyReviewed  = signal(false);
  reviewLoading    = signal(false);
  reviewError      = signal<string | null>(null);
  reviewSuccess    = signal<string | null>(null);
  hoveredStar      = signal(0);
  selectedRating   = signal(0);

  reviewForm: FormGroup;

  // Star labels shown on hover
  starLabels = ['', 'Poor', 'Fair', 'Good', 'Very Good', 'Excellent'];

  constructor(
    private route:          ActivatedRoute,
    private fb:             FormBuilder,
    private ticketService:  TicketService,
    private paymentService: PaymentService,
    private reviewService:  ReviewService,
    private authService:    AuthService
  ) {
    this.reviewForm = this.fb.group({
      comment: ['', Validators.maxLength(500)]
    });
  }

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
        if (ticket.status === 'REDEEMED') {
          this.checkReview(id);
        }
      },
      error: () => {
        this.errorMessage.set('Could not load this ticket.');
        this.loading.set(false);
      }
    });
  }

  private loadPayments(ticketId: number): void {
    this.paymentService.getCustomerPaymentHistory(ticketId).subscribe({
      next:  (p) => this.payments.set(p),
      error: ()  => this.payments.set([])
    });
  }

  private checkReview(ticketId: number): void {
    this.reviewService.hasReviewed(ticketId).subscribe({
      next: (has) => {
        this.alreadyReviewed.set(has);
        if (has) {
          // Load the existing review to display it
          this.reviewService.getShopReviews(this.ticket()!.shopId).subscribe({
            next: (reviews) => {
              const mine = reviews.find(r => r.ticketId === ticketId);
              if (mine) this.existingReview.set(mine);
            }
          });
        }
      },
      error: () => {}
    });
  }

  // ── Star rating interaction ───────────────────────────────────────────────────

  hoverStar(star: number): void  { this.hoveredStar.set(star); }
  clearHover(): void             { this.hoveredStar.set(0); }
  selectStar(star: number): void { this.selectedRating.set(star); }

  starClass(star: number): string {
    const active = this.hoveredStar() || this.selectedRating();
    return star <= active ? 'star-filled' : 'star-empty';
  }

  // ── Submit review ─────────────────────────────────────────────────────────────

  submitReview(): void {
    if (this.selectedRating() === 0) {
      this.reviewError.set('Please select a star rating.');
      return;
    }

    const customerId = this.authService.currentUser()?.customerId;
    if (!customerId) { this.reviewError.set('Not logged in as customer.'); return; }

    this.reviewLoading.set(true);
    this.reviewError.set(null);

    this.reviewService.submitReview(customerId, {
      ticketId: this.ticket()!.id,
      rating:   this.selectedRating(),
      comment:  this.reviewForm.get('comment')?.value || undefined
    }).subscribe({
      next: (review) => {
        this.reviewLoading.set(false);
        this.reviewSuccess.set('Thank you for your review!');
        this.alreadyReviewed.set(true);
        this.existingReview.set(review);
      },
      error: (err) => {
        this.reviewLoading.set(false);
        this.reviewError.set(err?.error?.message || 'Could not submit review.');
      }
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────────

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

  starsArray(n: number): number[] {
    return Array.from({ length: n }, (_, i) => i + 1);
  }
}