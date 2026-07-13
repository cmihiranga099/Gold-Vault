import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, IonContent,
  IonItem, IonInput, IonSelect, IonSelectOption, IonTextarea,
  IonButton, IonSpinner, IonText
} from '@ionic/angular/standalone';
import { MarketplaceService } from '../../core/services/marketplace.service';
import { AuthService } from '../../core/auth/auth.service';
import { GoldPurity } from '../../core/models/marketplace.model';

@Component({
  selector: 'app-create-listing',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    IonHeader, IonToolbar, IonTitle, IonButtons, IonBackButton, IonContent,
    IonItem, IonInput, IonSelect, IonSelectOption, IonTextarea,
    IonButton, IonSpinner, IonText
  ],
  templateUrl: './create-listing.page.html',
  styleUrl: './create-listing.page.scss'
})
export class CreateListingPage {
  form: FormGroup;
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  purities: GoldPurity[] = ['K24', 'K22', 'K21', 'K18', 'P916', 'P750', 'OTHER'];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private marketplaceService: MarketplaceService,
    private router: Router
  ) {
    this.form = this.fb.group({
      description: [''],
      weightGrams: [null as number | null, [Validators.required, Validators.min(0.001)]],
      purity: ['K22' as GoldPurity, Validators.required],
      askingPrice: [null as number | null]
    });
  }

  submit(): void {
    const customerId = this.authService.currentUser()?.customerId;
    if (this.form.invalid || !customerId) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    const raw = this.form.getRawValue();

    this.marketplaceService.createListing(customerId, {
      description: raw.description || undefined,
      weightGrams: raw.weightGrams!,
      purity: raw.purity!,
      askingPrice: raw.askingPrice ?? undefined
    }).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigateByUrl('/tabs/marketplace');
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message || 'Could not create listing.');
      }
    });
  }
}