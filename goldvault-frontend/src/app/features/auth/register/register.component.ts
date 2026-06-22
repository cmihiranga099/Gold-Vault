import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { AuthService } from '../../../core/auth/auth.service';

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
    MessageModule
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  form = this.fb.group({
    fullName: ['', Validators.required],
    username: ['', [Validators.required, Validators.minLength(4)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService.register(this.form.getRawValue() as {
      fullName: string; username: string; email: string; password: string;
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