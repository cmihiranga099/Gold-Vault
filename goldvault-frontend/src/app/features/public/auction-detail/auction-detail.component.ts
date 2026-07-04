import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuctionService } from '../../../core/services/auction.service';
import { AuctionResponse, AuctionBidResponse } from '../../../core/models/auction.model';

@Component({
  selector: 'app-auction-detail',
  standalone: true,
  imports: [
    CommonModule, RouterLink, ReactiveFormsModule,
    TagModule, ButtonModule, InputTextModule, InputNumberModule, MessageModule, ProgressSpinnerModule,
    TranslatePipe
  ],
  templateUrl: './auction-detail.component.html',
  styleUrl:    './auction-detail.component.scss'
})
export class AuctionDetailComponent implements OnInit {
  auction = signal<AuctionResponse | null>(null);
  bids    = signal<AuctionBidResponse[]>([]);
  loading = signal(true);
  error   = signal<string | null>(null);

  bidLoading = signal(false);
  bidError   = signal<string | null>(null);
  bidSuccess = signal<string | null>(null);

  bidForm: FormGroup;
  private auctionId!: number;

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private auctionService: AuctionService,
    private translate: TranslateService
  ) {
    this.bidForm = this.fb.group({
      bidderName:  ['', Validators.required],
      bidderPhone: ['', Validators.required],
      amount:      [null as number | null, [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    this.auctionId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  private load(): void {
    this.auctionService.getAuction(this.auctionId).subscribe({
      next: (a) => {
        this.auction.set(a);
        this.loading.set(false);
        const minBid = (a.currentBid ?? a.startingPrice) + 1;
        this.bidForm.patchValue({ amount: minBid });
        this.loadBids();
      },
      error: () => { this.error.set(this.translate.instant('auctionDetail.errorLoad')); this.loading.set(false); }
    });
  }

  private loadBids(): void {
    this.auctionService.getBids(this.auctionId).subscribe({
      next:  (b) => this.bids.set(b),
      error: ()  => this.bids.set([])
    });
  }

  submitBid(): void {
    if (this.bidForm.invalid) { this.bidForm.markAllAsTouched(); return; }

    this.bidLoading.set(true);
    this.bidError.set(null);
    this.bidSuccess.set(null);
    const raw = this.bidForm.getRawValue();

    this.auctionService.placeBid(this.auctionId, {
      bidderName:  raw.bidderName!,
      bidderPhone: raw.bidderPhone!,
      amount:      raw.amount!
    }).subscribe({
      next: (a) => {
        this.bidLoading.set(false);
        this.bidSuccess.set(this.translate.instant('auctionDetail.bidSuccess'));
        this.auction.set(a);
        this.loadBids();
        this.bidForm.patchValue({ amount: a.currentBid! + 1 });
      },
      error: (err) => {
        this.bidLoading.set(false);
        this.bidError.set(err?.error?.message || this.translate.instant('auctionDetail.bidError'));
      }
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }

  currentPrice(): number {
    const a = this.auction();
    return a ? (a.currentBid ?? a.startingPrice) : 0;
  }
}