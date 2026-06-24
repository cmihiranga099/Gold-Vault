import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AuthService } from '../../../core/auth/auth.service';
import { CustomerService } from '../../../core/services/customer.service';
import { CustomerResponse } from '../../../core/models/customer.model';

@Component({
  selector: 'app-shop-customers',
  standalone: true,
  imports: [
    CommonModule, RouterLink, FormsModule,
    ButtonModule, InputTextModule, TagModule, TableModule,
    TopnavComponent
  ],
  templateUrl: './customers.component.html',
  styleUrl: './customers.component.scss'
})
export class CustomersComponent implements OnInit {
  customers = signal<CustomerResponse[]>([]);
  totalRecords = signal(0);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  searchTerm = signal('');

  rows = 10;
  private shopId: number | null = null;
  private currentPage = 0;

  constructor(
    private authService: AuthService,
    private customerService: CustomerService
  ) {}

  ngOnInit(): void {
    this.shopId = this.authService.currentUser()?.shopId ?? null;

    if (!this.shopId) {
      this.errorMessage.set('No shop linked to this account.');
      this.loading.set(false);
    }
    // Initial load is triggered by p-table's (onLazyLoad) on first render
  }

  /** Called by p-table whenever page, sort, or filter changes */
  onLazyLoad(event: TableLazyLoadEvent): void {
    if (!this.shopId) return;

    const page = Math.floor((event.first ?? 0) / (event.rows ?? this.rows));
    const size = event.rows ?? this.rows;
    this.currentPage = page;
    this.loading.set(true);

    const term = this.searchTerm().trim();
    const request$ = term
      ? this.customerService.searchPaged(this.shopId, term, page, size)
      : this.customerService.getByShopPaged(this.shopId, page, size);

    request$.subscribe({
      next: (result) => {
        this.customers.set(result.content);
        this.totalRecords.set(result.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not load customers.');
        this.loading.set(false);
      }
    });
  }

  onSearch(): void {
    // Reset to first page whenever the search term changes
    this.onLazyLoad({ first: 0, rows: this.rows });
  }

  kycSeverity(status: string): 'success' | 'warn' | 'danger' {
    switch (status) {
      case 'VERIFIED': return 'success';
      case 'REJECTED': return 'danger';
      default: return 'warn';
    }
  }
}