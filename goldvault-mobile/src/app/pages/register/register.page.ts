import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  IonContent, IonItem, IonInput, IonSelect, IonSelectOption,
  IonButton, IonSpinner, IonText, IonIcon, IonBackButton, IonButtons,
  IonHeader, IonToolbar, IonTitle
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  personOutline, mailOutline, lockClosedOutline, cardOutline,
  callOutline, businessOutline
} from 'ionicons/icons';
import { AuthService } from '../../core/auth/auth.service';
import { ShopService, ShopOption } from '../../core/services/shop.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule, RouterLink, ReactiveFormsModule,
    IonContent, IonItem, IonInput, IonSelect, IonSelectOption,
    IonButton, IonSpinner, IonText, IonIcon, IonBackButton, IonButtons,
    IonHeader, IonToolbar, IonTitle
  ],
  templateUrl: './register.page.html',
  styleUrl: './register.page.scss'
})
export class RegisterPage implements OnInit {
  form: FormGroup;
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  shops = signal<ShopOption[]>([]);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private shopService: ShopService,
    private router: Router
  ) {
    addIcons({ personOutline, mailOutline, lockClosedOutline, cardOutline, callOutline, businessOutline });

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
    this.shopService.getActiveShopOptions().subscribe({
      next: (shops) => this.shops.set(shops),
      error: () => this.shops.set([])
    });
  }

  submit(): void {
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
        this.router.navigateByUrl('/tabs/dashboard');
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message || 'Registration failed. Please try again.');
      }
    });
  }
}