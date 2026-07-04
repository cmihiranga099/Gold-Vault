import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AmlService } from '../../../core/services/aml.service';
import { AmlFlagResponse, AmlSummary } from '../../../core/models/aml.model';

@Component({
  selector: 'app-aml-dashboard',
  standalone: true,
  imports: [
    CommonModule, RouterLink, FormsModule,
    TagModule, ButtonModule, DialogModule, MessageModule, ProgressSpinnerModule,
    TopnavComponent
  ],
  templateUrl: './aml.component.html',
  styleUrl:    './aml.component.scss'
})
export class AmlDashboardComponent implements OnInit {
  flags    = signal<AmlFlagResponse[]>([]);
  summary  = signal<AmlSummary | null>(null);
  loading  = signal(true);
  openOnly = signal(true);
  scanning = signal(false);
  scanMsg  = signal<string | null>(null);

  // Review dialog
  selectedFlag  = signal<AmlFlagResponse | null>(null);
  showReview    = false;
  reviewStatus  = 'REVIEWED';
  reviewNote    = '';
  reviewingBy   = 'Admin';
  reviewLoading = signal(false);

  constructor(private amlService: AmlService) {}

  ngOnInit(): void {
    this.load();
    this.amlService.getSummary().subscribe({
      next:  (s) => this.summary.set(s),
      error: ()  => {}
    });
  }

  load(): void {
    this.loading.set(true);
    this.amlService.getAllFlags(this.openOnly()).subscribe({
      next:  (f) => { this.flags.set(f); this.loading.set(false); },
      error: ()  => this.loading.set(false)
    });
  }

  toggleFilter(): void {
    this.openOnly.set(!this.openOnly());
    this.load();
  }

  triggerScan(): void {
    this.scanning.set(true);
    this.scanMsg.set(null);
    this.amlService.triggerScan().subscribe({
      next: (msg) => {
        this.scanning.set(false);
        this.scanMsg.set(msg);
        this.load();
        this.amlService.getSummary().subscribe(s => this.summary.set(s));
      },
      error: () => this.scanning.set(false)
    });
  }

  openReviewDialog(flag: AmlFlagResponse): void {
    this.selectedFlag.set(flag);
    this.reviewStatus = 'REVIEWED';
    this.reviewNote   = '';
    this.showReview   = true;
  }

  submitReview(): void {
    const flag = this.selectedFlag();
    if (!flag) return;
    this.reviewLoading.set(true);

    this.amlService.reviewFlag(flag.id, {
      status:     this.reviewStatus as 'REVIEWED' | 'DISMISSED',
      reviewNote: this.reviewNote || undefined,
      reviewedBy: this.reviewingBy
    }).subscribe({
      next: () => {
        this.reviewLoading.set(false);
        this.showReview = false;
        this.load();
        this.amlService.getSummary().subscribe(s => this.summary.set(s));
      },
      error: () => this.reviewLoading.set(false)
    });
  }

  flagSeverity(status: string): 'danger' | 'warn' | 'secondary' {
    if (status === 'OPEN')      return 'danger';
    if (status === 'REVIEWED')  return 'warn';
    return 'secondary';
  }

  typeSeverity(type: string): 'danger' | 'warn' | 'secondary' {
    if (type === 'LARGE_TRANSACTION') return 'danger';
    if (type === 'HIGH_VOLUME')       return 'danger';
    if (type === 'MULTIPLE_SHOPS')    return 'warn';
    return 'secondary';
  }

  formatCurrency(n: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(n);
  }
}