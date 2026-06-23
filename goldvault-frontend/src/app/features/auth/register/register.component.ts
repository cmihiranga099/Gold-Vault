import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';
import { AuthService } from '../../../core/auth/auth.service';
import { ShopOption, ShopService } from '../../../core/services/shop.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    MessageModule,
    SelectModule
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent implements OnInit {
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  shops = signal<ShopOption[]>([]);

  form: ReturnType<FormBuilder['group']>;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private shopService: ShopService,
    private router: Router
  ) {
    this.form = this.fb.group({
      fullName: ['', Validators.required],
      nic: ['', [Validators.required, Validators.pattern(/^([0-9]{9}[vVxX]|[0-9]{12})$/)]],
      shopId: [null as number | null, Validators.required],
      phone: [''],
      username: ['', [Validators.required, Validators.minLength(4)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  ngOnInit(): void {
    this.shopService.getActiveShops().subscribe({
      next: (shops) => this.shops.set(shops),
      error: () => this.shops.set([])
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();

    this.authService.register({
      fullName: raw.fullName!,
      nic: raw.nic!,
      shopId: raw.shopId!,
      phone: raw.phone || undefined,
      username: raw.username!,
      email: raw.email!,
      password: raw.password!
    }).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/customer/dashboard']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message || 'Registration failed. Please try again.');
      }
    });
  }
}