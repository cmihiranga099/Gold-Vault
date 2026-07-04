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
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
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
    TranslatePipe
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

  promoTypes: { label: string; value: PromoType }[] = [];

  form: FormGroup;
  private shopId!: number;

  constructor(
    private fb:               FormBuilder,
    private authService:      AuthService,
    private promotionService: PromotionService,
    private translate:        TranslateService
  ) {
    this.form = this.fb.group({
      title:       ['', Validators.required],
      description: [''],
      promoType:   ['REDUCED_INTEREST', Validators.required],
      promoValue:  [null as number | null],
      startsAt:    ['', Validators.required],
      endsAt:      ['', Validators.required]
    });

    this.promoTypes = [
      { label: this.translate.instant('promotionsPage.typeReducedInterest'), value: 'REDUCED_INTEREST' },
      { label: this.translate.instant('promotionsPage.typeBonusPoints'),     value: 'BONUS_POINTS'     },
      { label: this.translate.instant('promotionsPage.typeFreeRenewal'),     value: 'FREE_RENEWAL'     },
      { label: this.translate.instant('promotionsPage.typeCustom'),          value: 'CUSTOM'           }
    ];
  }

  ngOnInit(): void {
    this.shopId = this.authService.currentUser()?.shopId!;
    this.loadPromotions();
  }

  private loadPromotions(): void {
    this.loading.set(true);
    this.promotionService.getShopPromotions(this.shopId).subscribe({
      next:  (p) => { this.promotions.set(p); this.loading.set(false); },
      error: ()  => { this.error.set(this.translate.instant('promotionsPage.errLoad')); this.loading.set(false); }
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
        this.createError.set(err?.error?.message || this.translate.instant('promotionsPage.errCreate'));
      }
    });
  }

  cancelPromotion(promo: PromotionResponse): void {
    if (!confirm(this.translate.instant('promotionsPage.confirmCancel', { title: promo.title }))) return;
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
    if (p.currentlyActive) return `${this.translate.instant('promotionsPage.live')} · ${p.daysRemaining}${this.translate.instant('promotionsPage.daysLeftSuffix')}`;
    if (p.status === 'EXPIRED') return this.translate.instant('promotionsPage.expired');
    if (p.status === 'CANCELLED') return this.translate.instant('promotionsPage.cancelled');
    return this.translate.instant('promotionsPage.scheduled');
  }

  promoTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      REDUCED_INTEREST: 'promotionsPage.promoTypeReducedInterest',
      BONUS_POINTS:     'promotionsPage.promoTypeBonusPoints',
      FREE_RENEWAL:     'promotionsPage.promoTypeFreeRenewal',
      CUSTOM:           'promotionsPage.promoTypeCustom'
    };
    return labels[type] ? this.translate.instant(labels[type]) : type;
  }

  promoValueLabel(p: PromotionResponse): string {
    if (!p.promoValue) return '';
    if (p.promoType === 'REDUCED_INTEREST') return `${p.promoValue}${this.translate.instant('promotionsPage.interestRateSuffix')}`;
    if (p.promoType === 'BONUS_POINTS') return `${p.promoValue}${this.translate.instant('promotionsPage.bonusPointsSuffix')}`;
    return `${p.promoValue}`;
  }

  showPromoValue(): boolean {
    const t = this.form.get('promoType')?.value;
    return t === 'REDUCED_INTEREST' || t === 'BONUS_POINTS';
  }

  promoValueLabel2(): string {
    const t = this.form.get('promoType')?.value;
    if (t === 'REDUCED_INTEREST') return this.translate.instant('promotionsPage.promoValueRate');
    if (t === 'BONUS_POINTS') return this.translate.instant('promotionsPage.promoValuePoints');
    return this.translate.instant('promotionsPage.promoValueGeneric');
  }
}