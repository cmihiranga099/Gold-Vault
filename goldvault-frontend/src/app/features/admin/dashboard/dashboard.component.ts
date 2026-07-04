import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslatePipe } from '@ngx-translate/core';
import { AdminService } from '../../../core/services/admin.service';
import { DashboardSummaryResponse } from '../../../core/models/admin.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, ProgressSpinnerModule, TranslatePipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class AdminDashboardComponent implements OnInit {
  summary = signal<DashboardSummaryResponse | null>(null);
  loading = signal(true);
  errorMessage = signal<string | null>(null);

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getDashboard().subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not load dashboard data.');
        this.loading.set(false);
      }
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }

  // ── Trend chart helpers ─────────────────────────────────────────────────────

  months(map: Record<string, number>): string[] {
    return Object.keys(map ?? {});
  }

  shortMonth(ym: string): string {
    const [y, m] = ym.split('-');
    return new Date(+y, +m - 1, 1).toLocaleString('en-LK', { month: 'short' }) + ' ' + y.slice(2);
  }

  maxVal(map: Record<string, number>): number {
    return Math.max(...Object.values(map ?? {}), 1);
  }

  barHeight(val: number, max: number): string {
    if (!max) return '4px';
    return Math.max(4, Math.round((val / max) * 130)) + 'px';
  }

  npaSeverity(): 'success' | 'warn' | 'danger' {
    const rate = this.summary()?.npaRatePercent ?? 0;
    if (rate < 5) return 'success';
    if (rate < 15) return 'warn';
    return 'danger';
  }
}