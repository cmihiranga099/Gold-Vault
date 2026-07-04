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
  licenseUploading = signal(false);
  licenseUrl       = signal<string | null>(null);
  licenseFileName  = signal<string | null>(null);
  licenseError     = signal<string | null>(null);

  onLicenseSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    const allowed = ['image/jpeg','image/png','image/webp','application/pdf'];
    if (!allowed.includes(file.type)) {
      this.licenseError.set('Only PDF, JPEG, or PNG files allowed.');
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      this.licenseError.set('File must be under 10 MB.');
      return;
    }

    this.licenseError.set(null);
    this.licenseFileName.set(file.name);
    this.licenseUploading.set(true);

    const form = new FormData();
    form.append('file', file);

    this.http.post<any>(`${environment.apiUrl}/shop/upload/license-document`, form).subscribe({
      next: (res) => {
        this.licenseUrl.set(res.data.url);
        this.licenseUploading.set(false);
      },
      error: () => {
        this.licenseError.set('Upload failed. Try again.');
        this.licenseUploading.set(false);
      }
    });
  }

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
    if (this.licenseUploading()) { this.errorMessage.set('License is still uploading.'); return; }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.http.post<any>(`${environment.apiUrl}/shops/register`, {
      ...this.form.getRawValue(),
      licenseDocumentUrl: this.licenseUrl() ?? undefined
    }).subscribe({
      next:  ()    => { this.loading.set(false); this.success.set(true); },
      error: (err) => { this.loading.set(false); this.errorMessage.set(err?.error?.message || 'Registration failed.'); }
    });
  }
}