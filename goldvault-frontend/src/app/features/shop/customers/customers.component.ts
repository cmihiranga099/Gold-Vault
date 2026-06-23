import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AuthService } from '../../../core/auth/auth.service';
import { CustomerService } from '../../../core/services/customer.service';
import { CustomerResponse } from '../../../core/models/customer.model';

@Component({
  selector: 'app-shop-customers',
  standalone: true,
  imports: [
    CommonModule, RouterLink, FormsModule,
    ButtonModule, InputTextModule, ProgressSpinnerModule, TagModule,
    TopnavComponent
  ],
  templateUrl: './customers.component.html',
  styleUrl: './customers.component.scss'
})
export class CustomersComponent implements OnInit {
  customers = signal<CustomerResponse[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  searchTerm = signal('');

  private shopId: number | null = null;

  constructor(
    private authService: AuthService,
    private customerService: CustomerService
  ) {}

  ngOnInit(): void {
    this.shopId = this.authService.currentUser()?.shopId ?? null;

    if (!this.shopId) {
      this.errorMessage.set('No shop linked to this account.');
      this.loading.set(false);
      return;
    }

    this.loadCustomers();
  }

  loadCustomers(): void {
    if (!this.shopId) return;
    this.loading.set(true);

    this.customerService.getByShop(this.shopId).subscribe({
      next: (customers) => {
        this.customers.set(customers);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not load customers.');
        this.loading.set(false);
      }
    });
  }

  onSearch(): void {
    if (!this.shopId) return;
    const term = this.searchTerm().trim();

    if (!term) {
      this.loadCustomers();
      return;
    }

    this.loading.set(true);
    this.customerService.search(this.shopId, term).subscribe({
      next: (customers) => {
        this.customers.set(customers);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Search failed.');
        this.loading.set(false);
      }
    });
  }

  kycSeverity(status: string): 'success' | 'warn' | 'danger' {
    switch (status) {
      case 'VERIFIED': return 'success';
      case 'REJECTED': return 'danger';
      default: return 'warn';
    }
  }
}