import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { SelectModule } from 'primeng/select';
import { MessageModule } from 'primeng/message';
import { TooltipModule } from 'primeng/tooltip';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { environment } from '../../../../environments/environment';
import { TicketService } from '../../../core/services/ticket.service';
import { PaymentService } from '../../../core/services/payment.service';
import { PawnTicketResponse } from '../../../core/models/ticket.model';
import { PaymentResponse, PaymentSubmissionResponse, PaymentType, PaymentMethod } from '../../../core/models/payment.model';

@Component({
  selector: 'app-shop-ticket-detail',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    TagModule, ButtonModule, ProgressSpinnerModule, DialogModule,
    InputNumberModule, InputTextModule, TextareaModule, SelectModule, MessageModule, TooltipModule,
    TranslatePipe
  ],
  templateUrl: './ticket-detail.component.html',
  styleUrl: './ticket-detail.component.scss'
})
export class ShopTicketDetailComponent implements OnInit {
  ticket   = signal<PawnTicketResponse | null>(null);
  payments = signal<PaymentResponse[]>([]);
  loading  = signal(true);
  errorMessage = signal<string | null>(null);

  // ── Online payment submissions (bank transfer + receipt) ────────────────────
  submissions = signal<PaymentSubmissionResponse[]>([]);
  reviewingId = signal<number | null>(null);
  reviewError = signal<string | null>(null);
  showRejectDialog = false;
  rejectingSubmission: PaymentSubmissionResponse | null = null;
  rejectForm: ReturnType<FormBuilder['group']>;

  // ── Payment dialog ────────────────────────────────────────────────────────────
  showPaymentDialog = false;
  paymentLoading    = signal(false);
  paymentError      = signal<string | null>(null);
  paymentSuccess    = signal<string | null>(null);

  // ── Renewal dialog ────────────────────────────────────────────────────────────
  showRenewalDialog  = false;
  renewalLoading     = signal(false);
  renewalError       = signal<string | null>(null);
  renewalSuccess     = signal<string | null>(null);

  // ── PDF ───────────────────────────────────────────────────────────────────────
  pdfLoading = signal(false);
  pdfError   = signal<string | null>(null);

  paymentTypes = computed<{ label: string; value: PaymentType }[]>(() => [
    { label: this.translate.instant('ticketDetail.paymentTypes.INTEREST'),       value: 'INTEREST' },
    { label: this.translate.instant('ticketDetail.paymentTypes.PARTIAL'),        value: 'PARTIAL' },
    { label: this.translate.instant('ticketDetail.paymentTypes.FULL_REDEMPTION'), value: 'FULL_REDEMPTION' }
  ]);

  paymentMethods = computed<{ label: string; value: PaymentMethod }[]>(() => [
    { label: this.translate.instant('ticketDetail.paymentMethods.CASH'),            value: 'CASH' },
    { label: this.translate.instant('ticketDetail.paymentMethods.CARD'),            value: 'CARD' },
    { label: this.translate.instant('ticketDetail.paymentMethods.ONLINE_TRANSFER'), value: 'ONLINE_TRANSFER' },
    { label: this.translate.instant('ticketDetail.paymentMethods.LANKAQR'),         value: 'LANKAQR' }
  ]);

  paymentForm: ReturnType<FormBuilder['group']>;
  renewalForm: ReturnType<FormBuilder['group']>;

  private ticketId!: number;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private ticketService: TicketService,
    private paymentService: PaymentService,
    private translate: TranslateService
  ) {
    this.paymentForm = this.fb.group({
      amount:          [null as number | null, [Validators.required, Validators.min(0.01)]],
      paymentType:     ['INTEREST' as PaymentType, Validators.required],
      paymentMethod:   ['CASH' as PaymentMethod,   Validators.required],
      referenceNumber: ['']
    });

    this.renewalForm = this.fb.group({
      extensionMonths: [3,    [Validators.required, Validators.min(1), Validators.max(24)]],
      interestPaid:    [null as number | null, [Validators.required, Validators.min(0.01)]],
      paymentMethod:   ['CASH' as PaymentMethod, Validators.required],
      referenceNumber: ['']
    });

    this.rejectForm = this.fb.group({
      reason: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.ticketId = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.ticketId) {
      this.errorMessage.set('Invalid ticket.');
      this.loading.set(false);
      return;
    }
    this.loadTicket();
  }

  private loadTicket(): void {
    this.loading.set(true);
    this.ticketService.getShopTicketById(this.ticketId).subscribe({
      next: (ticket) => {
        this.ticket.set(ticket);
        this.loading.set(false);
        this.loadPayments();
        this.loadSubmissions();
        this.paymentForm.patchValue({ amount: ticket.outstandingBalance });
        // Pre-fill renewal interest field with today's accrued interest
        this.renewalForm.patchValue({ interestPaid: ticket.accruedInterestToday });
      },
      error: () => {
        this.errorMessage.set('Could not load this ticket.');
        this.loading.set(false);
      }
    });
  }

  private loadSubmissions(): void {
    this.paymentService.getSubmissionsForTicket(this.ticketId, true).subscribe({
      next: (s) => this.submissions.set(s),
      error: () => this.submissions.set([])
    });
  }

  // ── Online payment submissions (bank transfer + receipt) ────────────────────

  pendingSubmissions(): PaymentSubmissionResponse[] {
    return this.submissions().filter(s => s.status === 'PENDING');
  }

  approveSubmission(submission: PaymentSubmissionResponse): void {
    this.reviewingId.set(submission.id);
    this.reviewError.set(null);
    this.paymentService.approveSubmission(submission.id).subscribe({
      next: () => {
        this.reviewingId.set(null);
        this.loadTicket();
      },
      error: (err) => {
        this.reviewingId.set(null);
        this.reviewError.set(err?.error?.message || 'Could not approve this payment.');
      }
    });
  }

  openRejectDialog(submission: PaymentSubmissionResponse): void {
    this.rejectingSubmission = submission;
    this.rejectForm.reset();
    this.reviewError.set(null);
    this.showRejectDialog = true;
  }

  confirmReject(): void {
    if (this.rejectForm.invalid || !this.rejectingSubmission) {
      this.rejectForm.markAllAsTouched();
      return;
    }
    const submission = this.rejectingSubmission;
    this.reviewingId.set(submission.id);
    this.paymentService.rejectSubmission(submission.id, this.rejectForm.getRawValue().reason!).subscribe({
      next: () => {
        this.reviewingId.set(null);
        this.showRejectDialog = false;
        this.loadSubmissions();
      },
      error: (err) => {
        this.reviewingId.set(null);
        this.reviewError.set(err?.error?.message || 'Could not reject this payment.');
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

  receiptUrl(relativePath: string): string {
    // environment.apiUrl is e.g. "http://localhost:8080/api" — uploaded files
    // are served from the app root ("/uploads/..."), not under /api.
    const base = environment.apiUrl.replace(/\/api\/?$/, '');
    return `${base}/${relativePath}`;
  }

  private loadPayments(): void {
    this.paymentService.getShopPaymentHistory(this.ticketId).subscribe({
      next: (payments) => this.payments.set(payments),
      error: () => this.payments.set([])
    });
  }

  // ── Payment ───────────────────────────────────────────────────────────────────

  openPaymentDialog(): void {
    this.paymentError.set(null);
    this.paymentSuccess.set(null);
    this.showPaymentDialog = true;
  }

  submitPayment(): void {
    if (this.paymentForm.invalid) { this.paymentForm.markAllAsTouched(); return; }
    this.paymentLoading.set(true);
    this.paymentError.set(null);
    const raw = this.paymentForm.getRawValue();

    this.paymentService.recordPayment({
      ticketId:        this.ticketId,
      amount:          raw.amount!,
      paymentType:     raw.paymentType!,
      paymentMethod:   raw.paymentMethod!,
      referenceNumber: raw.referenceNumber || undefined
    }).subscribe({
      next: (payment) => {
        this.paymentLoading.set(false);
        this.paymentSuccess.set(
          payment.ticketRedeemed
            ? this.translate.instant('ticketDetail.paymentSuccessRedeemed')
            : this.translate.instant('ticketDetail.paymentSuccess')
        );
        this.loadTicket();
        setTimeout(() => { this.showPaymentDialog = false; this.paymentSuccess.set(null); }, 1500);
      },
      error: (err) => {
        this.paymentLoading.set(false);
        this.paymentError.set(err?.error?.message || 'Could not record payment.');
      }
    });
  }

  // ── Renewal ───────────────────────────────────────────────────────────────────

  openRenewalDialog(): void {
    this.renewalError.set(null);
    this.renewalSuccess.set(null);
    // Always refresh the interest amount when opening
    const t = this.ticket();
    if (t) this.renewalForm.patchValue({ interestPaid: t.accruedInterestToday });
    this.showRenewalDialog = true;
  }

  submitRenewal(): void {
    if (this.renewalForm.invalid) { this.renewalForm.markAllAsTouched(); return; }
    this.renewalLoading.set(true);
    this.renewalError.set(null);
    const raw = this.renewalForm.getRawValue();

    this.ticketService.renewTicket(this.ticketId, {
      extensionMonths: raw.extensionMonths!,
      interestPaid:    raw.interestPaid!,
      paymentMethod:   raw.paymentMethod!,
      referenceNumber: raw.referenceNumber || undefined
    }).subscribe({
      next: (ticket) => {
        this.renewalLoading.set(false);
        this.renewalSuccess.set(
          `Ticket extended to ${new Date(ticket.expiryDate).toLocaleDateString('en-LK', { day:'numeric', month:'short', year:'numeric' })}`
        );
        this.ticket.set(ticket);
        this.loadPayments();
        setTimeout(() => { this.showRenewalDialog = false; this.renewalSuccess.set(null); }, 2000);
      },
      error: (err) => {
        this.renewalLoading.set(false);
        this.renewalError.set(err?.error?.message || 'Could not extend ticket.');
      }
    });
  }

  // ── PDF ───────────────────────────────────────────────────────────────────────

  downloadReceipt(): void {
    this.pdfLoading.set(true);
    this.pdfError.set(null);
    this.ticketService.downloadReceipt(this.ticketId).subscribe({
      next: (response) => {
        this.pdfLoading.set(false);
        const blob = response.body!;
        const url  = window.URL.createObjectURL(blob);
        const a    = document.createElement('a');
        a.href     = url;
        a.download = `pawn-receipt-${this.ticket()?.ticketNumber ?? this.ticketId}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.pdfLoading.set(false);
        this.pdfError.set('Could not generate receipt. Please try again.');
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
}