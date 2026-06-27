import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AuthService } from '../../../core/auth/auth.service';
import { ShopDashboardService } from '../../../core/services/shop-dashboard.service';
import { ShopDashboardResponse } from '../../../core/models/dashboard.model';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-shop-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, ProgressSpinnerModule, TranslatePipe, TopnavComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class ShopDashboardComponent implements OnInit {
  summary = signal<ShopDashboardResponse | null>(null);
  loading = signal(true);
  errorMessage = signal<string | null>(null);

  constructor(
    private authService: AuthService,
    private shopDashboardService: ShopDashboardService
  ) {}

  ngOnInit(): void {
    const shopId = this.authService.currentUser()?.shopId;

    if (!shopId) {
      this.errorMessage.set('No shop linked to this account.');
      this.loading.set(false);
      return;
    }

    this.shopDashboardService.getSummary(shopId).subscribe({
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