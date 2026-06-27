import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslatePipe } from '@ngx-translate/core';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AdminService } from '../../../core/services/admin.service';
import { DashboardSummaryResponse } from '../../../core/models/admin.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, ProgressSpinnerModule, TranslatePipe, TopnavComponent],
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
}