import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { TranslatePipe } from '@ngx-translate/core';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AuthService } from '../../../core/auth/auth.service';
import { CustomerService } from '../../../core/services/customer.service';

// ── Sri Lanka NIC validator ───────────────────────────────────────────────────
function sriLankaNicValidator(control: AbstractControl): ValidationErrors | null {
  const val: string = (control.value || '').trim().toUpperCase();
  if (!val) return null; // let required handle empty
  // Old format: 9 digits + V or X
  const oldNic = /^[0-9]{9}[VX]$/.test(val);
  // New format: 12 digits
  const newNic = /^[0-9]{12}$/.test(val);
  if (!oldNic && !newNic) {
    return { invalidNic: true };
  }
  // Old NIC birth year logic: digits 1-3 = year offset (add 1900)
  // New NIC: digits 1-4 = birth year
  if (oldNic) {
    const yearPart = parseInt(val.substring(0, 3), 10);
    const year = 1900 + yearPart;
    if (year < 1900 || year > new Date().getFullYear()) return { invalidNic: true };
  }
  if (newNic) {
    const year = parseInt(val.substring(0, 4), 10);
    if (year < 1900 || year > new Date().getFullYear()) return { invalidNic: true };
  }
  return null;
}

@Component({
  selector: 'app-customer-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    InputTextModule, ButtonModule, MessageModule,
    TranslatePipe, TopnavComponent
  ],
  templateUrl: './customer-form.component.html',
  styleUrl: './customer-form.component.scss'
})
export class CustomerFormComponent {
  loading    = signal(false);
  errorMessage = signal<string | null>(null);

  // NIC photo state
  nicPhotoFile   = signal<File | null>(null);
  nicPhotoPreview = signal<string | null>(null);
  nicPhotoUrl    = signal<string | null>(null);
  nicPhotoUploading = signal(false);
  nicPhotoError  = signal<string | null>(null);

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private customerService: CustomerService,
    private router: Router
  ) {
    this.form = this.fb.group({
      fullName: ['', Validators.required],
      nic:      ['', [Validators.required, sriLankaNicValidator]],
      phone:    [''],
      email:    ['', Validators.email],
      address:  ['']
    });
  }

  // ── NIC helpers ──────────────────────────────────────────────────────────────

  get nicControl() { return this.form.get('nic')!; }

  get nicFormatHint(): string {
    const val = (this.nicControl.value || '').trim().toUpperCase();
    if (!val) return '';
    if (/^[0-9]{12}$/.test(val)) return '✓ New format (12 digits)';
    if (/^[0-9]{9}[VX]$/.test(val)) return '✓ Old format (9 digits + V/X)';
    if (val.length > 0 && val.length < 9) return 'Keep typing…';
    return '';
  }

  get nicError(): string {
    if (this.nicControl.touched && this.nicControl.hasError('required')) return 'NIC is required.';
    if (this.nicControl.touched && this.nicControl.hasError('invalidNic')) {
      return 'Enter a valid NIC: 9 digits + V or X (old), or 12 digits (new).';
    }
    return '';
  }

  // ── NIC photo ────────────────────────────────────────────────────────────────

 // ── NIC photo with OCR ──────────────────────────────────────────────────────

 ocrScanning = signal(false);
 ocrMessage  = signal<string | null>(null);
 ocrDetected = signal(false);

 onNicPhotoSelected(event: Event): void {
   const input = event.target as HTMLInputElement;
   const file = input.files?.[0];
   if (!file) return;

   // Client-side validation
   if (!['image/jpeg', 'image/jpg', 'image/png', 'image/webp'].includes(file.type)) {
     this.nicPhotoError.set('Only JPEG, PNG, or WEBP images allowed.');
     return;
   }
   if (file.size > 5 * 1024 * 1024) {
     this.nicPhotoError.set('File must be under 5 MB.');
     return;
   }

   this.nicPhotoError.set(null);
   this.ocrMessage.set(null);
   this.ocrDetected.set(false);
   this.nicPhotoFile.set(file);

   // Preview
   const reader = new FileReader();
   reader.onload = (e) => this.nicPhotoPreview.set(e.target?.result as string);
   reader.readAsDataURL(file);

   // Upload + OCR scan in one call
   this.nicPhotoUploading.set(true);
   this.ocrScanning.set(true);

   this.customerService.uploadNicPhotoWithOcr(file).subscribe({
     next: (res) => {
       this.nicPhotoUrl.set(res.photoUrl);
       this.nicPhotoUploading.set(false);
       this.ocrScanning.set(false);
       this.ocrMessage.set(res.message);

       if (res.ocrSuccess && res.nic) {
         this.ocrDetected.set(true);
         // Auto-fill the NIC field — staff can still edit if OCR got it wrong
         this.form.patchValue({ nic: res.nic });
         this.nicControl.markAsTouched();
       }
     },
     error: () => {
       this.nicPhotoError.set('Upload failed. Try again.');
       this.nicPhotoUploading.set(false);
       this.ocrScanning.set(false);
     }
   });
 }

 removeNicPhoto(): void {
  this.nicPhotoFile.set(null);
  this.nicPhotoPreview.set(null);
  this.nicPhotoUrl.set(null);
  this.nicPhotoError.set(null);
  this.ocrMessage.set(null);
  this.ocrDetected.set(false);
}
  // ── Submit ───────────────────────────────────────────────────────────────────

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    const shopId = this.authService.currentUser()?.shopId;
    if (!shopId) { this.errorMessage.set('No shop linked to this account.'); return; }

    if (this.nicPhotoUploading()) {
      this.errorMessage.set('NIC photo is still uploading. Please wait.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    const raw = this.form.getRawValue();

    this.customerService.register(shopId, {
      fullName:    raw.fullName,
      nic:         raw.nic.trim().toUpperCase(),
      phone:       raw.phone   || undefined,
      email:       raw.email   || undefined,
      address:     raw.address || undefined,
      nicPhotoUrl: this.nicPhotoUrl() ?? undefined
    }).subscribe({
      next: (customer) => { this.loading.set(false); this.router.navigate(['/shop/customers', customer.id]); },
      error: (err) => { this.loading.set(false); this.errorMessage.set(err?.error?.message || 'Could not register customer.'); }
    });
  }
}