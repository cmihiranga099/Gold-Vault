import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-shop-register',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    InputTextModule, ButtonModule, MessageModule
  ],
  templateUrl: './shop-register.component.html',
  styleUrl:    './shop-register.component.scss'
})
export class ShopRegisterComponent {
  loading      = signal(false);
  success      = signal(false);
  errorMessage = signal<string | null>(null);

  form: FormGroup;

  constructor(
    private fb:     FormBuilder,
    private http:   HttpClient,
    private router: Router
  ) {
    this.form = this.fb.group({
      name:      ['', Validators.required],
      regNumber: ['', Validators.required],
      ownerName: ['', Validators.required],
      phone:     [''],
      email:     ['', Validators.email],
      address:   ['']
    });
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.errorMessage.set(null);

    this.http.post<any>(`${environment.apiUrl}/shops/register`, this.form.getRawValue()).subscribe({
      next:  ()    => { this.loading.set(false); this.success.set(true); },
      error: (err) => { this.loading.set(false); this.errorMessage.set(err?.error?.message || 'Registration failed.'); }
    });
  }
}