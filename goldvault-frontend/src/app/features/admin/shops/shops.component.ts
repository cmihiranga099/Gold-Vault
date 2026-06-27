import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AdminService } from '../../../core/services/admin.service';
import { ShopResponse, ShopStatus } from '../../../core/models/admin.model';

@Component({
  selector: 'app-admin-shops',
  standalone: true,
  imports: [CommonModule, TagModule, ButtonModule, ProgressSpinnerModule, TopnavComponent],
  templateUrl: './shops.component.html',
  styleUrl: './shops.component.scss'
})
export class AdminShopsComponent implements OnInit {
  shops = signal<ShopResponse[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  actionLoading = signal<number | null>(null);

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadShops();
  }

   loadShops(): void {
    this.loading.set(true);
    this.adminService.getAllShops().subscribe({
      next: (shops) => {
        const order: Record<ShopStatus, number> = { PENDING: 0, ACTIVE: 1, SUSPENDED: 2 };
        this.shops.set([...shops].sort((a, b) => order[a.status] - order[b.status]));
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not load shops.');
        this.loading.set(false);
      }
    });
  }

  approve(id: number): void {
    this.actionLoading.set(id);
    this.adminService.approveShop(id).subscribe({
      next: () => {
        this.actionLoading.set(null);
        this.loadShops();
      },
      error: () => this.actionLoading.set(null)
    });
  }

  suspend(id: number): void {
    this.actionLoading.set(id);
    this.adminService.suspendShop(id).subscribe({
      next: () => {
        this.actionLoading.set(null);
        this.loadShops();
      },
      error: () => this.actionLoading.set(null)
    });
  }

  reactivate(id: number): void {
    this.actionLoading.set(id);
    this.adminService.reactivateShop(id).subscribe({
      next: () => {
        this.actionLoading.set(null);
        this.loadShops();
      },
      error: () => this.actionLoading.set(null)
    });
  }

  statusSeverity(status: ShopStatus): 'success' | 'warn' | 'danger' {
    switch (status) {
      case 'ACTIVE': return 'success';
      case 'SUSPENDED': return 'danger';
      default: return 'warn';
    }
  }
}