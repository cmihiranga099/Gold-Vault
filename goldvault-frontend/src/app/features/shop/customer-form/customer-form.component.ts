import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { TranslatePipe } from '@ngx-translate/core';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AuthService } from '../../../core/auth/auth.service';
import { CustomerService } from '../../../core/services/customer.service';

@Component({
  selector: 'app-customer-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    InputTextModule, ButtonModule, MessageModule,
    TranslatePipe,
    TopnavComponent
  ],
  templateUrl: './customer-form.component.html',
  styleUrl: './customer-form.component.scss'
})
export class CustomerFormComponent {
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private customerService: CustomerService,
    private router: Router
  ) {
    this.form = this.fb.group({
      fullName: ['', Validators.required],
      nic: ['', [Validators.required, Validators.pattern(/^([0-9]{9}[vVxX]|[0-9]{12})$/)]],
      phone: [''],
      email: ['', Validators.email],
      address: ['']
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const shopId = this.authService.currentUser()?.shopId;
    if (!shopId) {
      this.errorMessage.set('No shop linked to this account.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();

    this.customerService.register(shopId, {
      fullName: raw.fullName!,
      nic: raw.nic!,
      phone: raw.phone || undefined,
      email: raw.email || undefined,
      address: raw.address || undefined
    }).subscribe({
      next: (customer) => {
        this.loading.set(false);
        this.router.navigate(['/shop/customers', customer.id]);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message || 'Could not register customer.');
      }
    });
  }
}