import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { MessageModule } from 'primeng/message';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AuthService } from '../../../core/auth/auth.service';
import { PromotionService } from '../../../core/services/promotion.service';
import { PromotionResponse, PromoType } from '../../../core/models/promotion.model';

@Component({
  selector: 'app-shop-promotions',
  standalone: true,
  imports: [
    CommonModule, RouterLink, ReactiveFormsModule,
    ButtonModule, InputTextModule, InputNumberModule,
    SelectModule, MessageModule, TagModule, DialogModule,
    TopnavComponent
  ],
  templateUrl: './promotions.component.html',
  styleUrl:    './promotions.component.scss'
})
export class ShopPromotionsComponent implements OnInit {
  promotions = signal<PromotionResponse[]>([]);
  loading    = signal(true);
  error      = signal<string | null>(null);

  showDialog  = false;
  creating    = signal(false);
  createError = signal<string | null>(null);

  promoTypes: { label: string; value: PromoType }[] = [
    { label: 'Reduced interest rate',  value: 'REDUCED_INTEREST' },
    { label: 'Bonus loyalty points',   value: 'BONUS_POINTS'     },
    { label: 'Free ticket renewal',    value: 'FREE_RENEWAL'     },
    { label: 'Custom promotion',       value: 'CUSTOM'           }
  ];

  form: FormGroup;
  private shopId!: number;

  constructor(
    private fb:               FormBuilder,
    private authService:      AuthService,
    private promotionService: PromotionService
  ) {
    this.form = this.fb.group({
      title:       ['', Validators.required],
      description: [''],
      promoType:   ['REDUCED_INTEREST', Validators.required],
      promoValue:  [null as number | null],
      startsAt:    ['', Validators.required],
      endsAt:      ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.shopId = this.authService.currentUser()?.shopId!;
    this.loadPromotions();
  }

  private loadPromotions(): void {
    this.loading.set(true);
    this.promotionService.getShopPromotions(this.shopId).subscribe({
      next:  (p) => { this.promotions.set(p); this.loading.set(false); },
      error: ()  => { this.error.set('Could not load promotions.'); this.loading.set(false); }
    });
  }

  openDialog(): void {
    this.createError.set(null);
    this.form.reset({ promoType: 'REDUCED_INTEREST' });
    this.showDialog = true;
  }

  submitPromotion(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.creating.set(true);
    this.createError.set(null);
    const raw = this.form.getRawValue();

    this.promotionService.createPromotion(this.shopId, {
      title:       raw.title,
      description: raw.description || undefined,
      promoType:   raw.promoType,
      promoValue:  raw.promoValue || undefined,
      startsAt:    new Date(raw.startsAt).toISOString(),
      endsAt:      new Date(raw.endsAt).toISOString()
    }).subscribe({
      next: () => {
        this.creating.set(false);
        this.showDialog = false;
        this.loadPromotions();
      },
      error: (err) => {
        this.creating.set(false);
        this.createError.set(err?.error?.message || 'Could not create promotion.');
      }
    });
  }

  cancelPromotion(promo: PromotionResponse): void {
    if (!confirm(`Cancel "${promo.title}"?`)) return;
    this.promotionService.cancelPromotion(promo.id, this.shopId).subscribe({
      next:  () => this.loadPromotions(),
      error: () => {}
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  statusSeverity(p: PromotionResponse): 'success' | 'warn' | 'secondary' | 'danger' {
    if (p.currentlyActive) return 'success';
    if (p.status === 'EXPIRED') return 'secondary';
    if (p.status === 'CANCELLED') return 'danger';
    return 'warn';
  }

  statusLabel(p: PromotionResponse): string {
    if (p.currentlyActive) return `Live · ${p.daysRemaining}d left`;
    if (p.status === 'EXPIRED') return 'Expired';
    if (p.status === 'CANCELLED') return 'Cancelled';
    return 'Scheduled';
  }

  promoTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      REDUCED_INTEREST: 'Reduced interest',
      BONUS_POINTS:     'Bonus points',
      FREE_RENEWAL:     'Free renewal',
      CUSTOM:           'Custom'
    };
    return labels[type] ?? type;
  }

  promoValueLabel(p: PromotionResponse): string {
    if (!p.promoValue) return '';
    if (p.promoType === 'REDUCED_INTEREST') return `${p.promoValue}% interest rate`;
    if (p.promoType === 'BONUS_POINTS') return `${p.promoValue}x bonus points`;
    return `${p.promoValue}`;
  }

  showPromoValue(): boolean {
    const t = this.form.get('promoType')?.value;
    return t === 'REDUCED_INTEREST' || t === 'BONUS_POINTS';
  }

  promoValueLabel2(): string {
    const t = this.form.get('promoType')?.value;
    if (t === 'REDUCED_INTEREST') return 'Promotional interest rate (%)';
    if (t === 'BONUS_POINTS') return 'Points multiplier';
    return 'Value';
  }
}