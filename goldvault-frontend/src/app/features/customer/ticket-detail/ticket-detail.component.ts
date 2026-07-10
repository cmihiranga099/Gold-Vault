import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ButtonModule } from 'primeng/button';
import { TextareaModule } from 'primeng/textarea';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { MessageModule } from 'primeng/message';
import { TranslatePipe } from '@ngx-translate/core';
import { TicketService } from '../../../core/services/ticket.service';
import { PaymentService } from '../../../core/services/payment.service';
import { ReviewService } from '../../../core/services/review.service';
import { AuthService } from '../../../core/auth/auth.service';
import { PawnTicketResponse } from '../../../core/models/ticket.model';
import { PaymentResponse, PaymentSubmissionResponse, PaymentType } from '../../../core/models/payment.model';
import { ReviewResponse } from '../../../core/models/review.model';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [
    CommonModule, RouterLink, ReactiveFormsModule,
    TagModule, ProgressSpinnerModule, ButtonModule,
    TextareaModule, InputTextModule, InputNumberModule, SelectModule, MessageModule,
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

  // ── Online payment (bank transfer + receipt) ────────────────────────────────
  submissions       = signal<PaymentSubmissionResponse[]>([]);
  showPayOnlineForm = signal(false);
  selectedReceipt   = signal<File | null>(null);
  payOnlineLoading  = signal(false);
  payOnlineError    = signal<string | null>(null);
  payOnlineSuccess  = signal<string | null>(null);

  payOnlineForm: FormGroup;

  paymentTypes: { label: string; value: PaymentType }[] = [
    { label: 'Interest', value: 'INTEREST' },
    { label: 'Partial payment', value: 'PARTIAL' },
    { label: 'Full redemption', value: 'FULL_REDEMPTION' }
  ];

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

  private ticketId!: number;

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

    this.payOnlineForm = this.fb.group({
      amount:          [null as number | null, [Validators.required, Validators.min(0.01)]],
      paymentType:     ['INTEREST' as PaymentType, Validators.required],
      bankName:        [''],
      referenceNumber: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.ticketId = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.ticketId) {
      this.errorMessage.set('Invalid ticket.');
      this.loading.set(false);
      return;
    }

    this.ticketService.getTicketDetail(this.ticketId).subscribe({
      next: (ticket) => {
        this.ticket.set(ticket);
        this.loading.set(false);
        this.loadPayments(this.ticketId);
        this.loadSubmissions();
        this.payOnlineForm.patchValue({ amount: ticket.outstandingBalance });
        if (ticket.status === 'REDEEMED') {
          this.checkReview(this.ticketId);
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

  private loadSubmissions(): void {
    this.paymentService.getSubmissionsForTicket(this.ticketId).subscribe({
      next:  (s) => this.submissions.set(s),
      error: ()  => this.submissions.set([])
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

  // ── Online payment (bank transfer + receipt) ────────────────────────────────

  canPayOnline(): boolean {
    const status = this.ticket()?.status;
    return status === 'ACTIVE' || status === 'EXPIRED';
  }

  hasPendingSubmission(): boolean {
    return this.submissions().some(s => s.status === 'PENDING');
  }

  togglePayOnlineForm(): void {
    this.payOnlineError.set(null);
    this.payOnlineSuccess.set(null);
    this.showPayOnlineForm.update(v => !v);
  }

  onReceiptSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedReceipt.set(input.files?.[0] ?? null);
  }

  submitOnlinePayment(): void {
    if (this.payOnlineForm.invalid || !this.selectedReceipt()) {
      this.payOnlineForm.markAllAsTouched();
      if (!this.selectedReceipt()) {
        this.payOnlineError.set('Please attach a photo or PDF of your bank transfer receipt.');
      }
      return;
    }

    const customerId = this.authService.currentUser()?.customerId;
    if (!customerId) { this.payOnlineError.set('Not logged in as customer.'); return; }

    const raw = this.payOnlineForm.getRawValue();
    this.payOnlineLoading.set(true);
    this.payOnlineError.set(null);

    this.paymentService.submitOnlinePayment(customerId, {
      ticketId:        this.ticketId,
      amount:          raw.amount!,
      paymentType:     raw.paymentType!,
      bankName:        raw.bankName || undefined,
      referenceNumber: raw.referenceNumber!,
      receipt:         this.selectedReceipt()!
    }).subscribe({
      next: () => {
        this.payOnlineLoading.set(false);
        this.payOnlineSuccess.set('Payment submitted — the shop will confirm it shortly.');
        this.loadSubmissions();
        this.payOnlineForm.reset({ amount: this.ticket()?.outstandingBalance, paymentType: 'INTEREST' });
        this.selectedReceipt.set(null);
        setTimeout(() => { this.showPayOnlineForm.set(false); this.payOnlineSuccess.set(null); }, 2000);
      },
      error: (err) => {
        this.payOnlineLoading.set(false);
        this.payOnlineError.set(err?.error?.message || 'Could not submit payment.');
      }
    });
  }

  submissionSeverity(status: string): 'success' | 'warn' | 'danger' | 'secondary' {
    switch (status) {
      case 'APPROVED': return 'success';
      case 'REJECTED': return 'danger';
      default:         return 'warn';
    }
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