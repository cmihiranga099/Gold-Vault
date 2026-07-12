import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, IonContent,
  IonSpinner, IonText, IonBadge, IonItem, IonInput, IonSelect, IonSelectOption,
  IonButton, IonIcon, IonList, IonLabel
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { businessOutline, attachOutline, checkmarkOutline, timeOutline } from 'ionicons/icons';
import { AuthService } from '../../core/auth/auth.service';
import { TicketService } from '../../core/services/ticket.service';
import { PaymentService } from '../../core/services/payment.service';
import { PawnTicketResponse, TicketStatus } from '../../core/models/ticket.model';
import { PaymentResponse, PaymentSubmissionResponse, PaymentType, SubmissionStatus } from '../../core/models/payment.model';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, IonContent,
    IonSpinner, IonText, IonBadge, IonItem, IonInput, IonSelect, IonSelectOption,
    IonButton, IonIcon, IonList, IonLabel
  ],
  templateUrl: './ticket-detail.page.html',
  styleUrl: './ticket-detail.page.scss'
})
export class TicketDetailPage implements OnInit {
  ticket = signal<PawnTicketResponse | null>(null);
  payments = signal<PaymentResponse[]>([]);
  submissions = signal<PaymentSubmissionResponse[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);

  // Pay online
  showPayOnlineForm = signal(false);
  selectedReceipt = signal<File | null>(null);
  payOnlineLoading = signal(false);
  payOnlineError = signal<string | null>(null);
  payOnlineSuccess = signal<string | null>(null);
  payOnlineForm: FormGroup;

  private ticketId!: number;

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private authService: AuthService,
    private ticketService: TicketService,
    private paymentService: PaymentService
  ) {
    addIcons({ businessOutline, attachOutline, checkmarkOutline, timeOutline });

    this.payOnlineForm = this.fb.group({
      amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
      paymentType: ['INTEREST' as PaymentType, Validators.required],
      bankName: [''],
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
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.ticketService.getTicketDetail(this.ticketId).subscribe({
      next: (ticket) => {
        this.ticket.set(ticket);
        this.loading.set(false);
        this.loadPayments();
        this.loadSubmissions();
        this.payOnlineForm.patchValue({ amount: ticket.outstandingBalance });
      },
      error: () => {
        this.errorMessage.set('Could not load this ticket.');
        this.loading.set(false);
      }
    });
  }

  private loadPayments(): void {
    this.paymentService.getCustomerPaymentHistory(this.ticketId).subscribe({
      next: (p) => this.payments.set(p),
      error: () => this.payments.set([])
    });
  }

  private loadSubmissions(): void {
    this.paymentService.getSubmissionsForTicket(this.ticketId).subscribe({
      next: (s) => this.submissions.set(s),
      error: () => this.submissions.set([])
    });
  }

  // ── Pay online ─────────────────────────────────────────────────────────────

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
      ticketId: this.ticketId,
      amount: raw.amount!,
      paymentType: raw.paymentType!,
      bankName: raw.bankName || undefined,
      referenceNumber: raw.referenceNumber!,
      receipt: this.selectedReceipt()!
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

  // ── Helpers ───────────────────────────────────────────────────────────────

  statusColor(status: TicketStatus): string {
    switch (status) {
      case 'ACTIVE': return 'success';
      case 'EXPIRED': return 'warning';
      case 'AUCTIONED': return 'danger';
      default: return 'medium';
    }
  }

  submissionColor(status: SubmissionStatus): string {
    switch (status) {
      case 'APPROVED': return 'success';
      case 'REJECTED': return 'danger';
      default: return 'warning';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }
}