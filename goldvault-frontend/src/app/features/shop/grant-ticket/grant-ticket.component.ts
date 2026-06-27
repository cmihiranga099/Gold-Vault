import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AuthService } from '../../../core/auth/auth.service';
import { CustomerService } from '../../../core/services/customer.service';
import { TicketService } from '../../../core/services/ticket.service';
import { CustomerResponse } from '../../../core/models/customer.model';
import { PawnTicketResponse } from '../../../core/models/ticket.model';

const GOLD_TYPES     = ['NECKLACE', 'RING', 'BANGLE', 'EARRING', 'CHAIN', 'OTHER'];
const GOLD_PURITIES  = ['K24', 'K22', 'K21', 'K18', 'P916', 'P750', 'OTHER'];

interface ItemPhotoState {
  preview:   string | null;
  url:       string | null;
  uploading: boolean;
  error:     string | null;
}

@Component({
  selector: 'app-grant-ticket',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    InputTextModule, InputNumberModule, SelectModule, ButtonModule, MessageModule,
    TranslatePipe, TopnavComponent
  ],
  templateUrl: './grant-ticket.component.html',
  styleUrl: './grant-ticket.component.scss'
})
export class GrantTicketComponent implements OnInit {
  loading       = signal(false);
  errorMessage  = signal<string | null>(null);
  customer      = signal<CustomerResponse | null>(null);
  createdTicket = signal<PawnTicketResponse | null>(null);

  goldTypes     = GOLD_TYPES;
  goldPurities  = GOLD_PURITIES;

  // Per-item photo state array, mirrors goldItemsArray
  itemPhotos: ItemPhotoState[] = [this.emptyPhotoState()];

  interestTypeOptions = computed(() => [
    { label: this.translate.instant('grantTicket.interestTypeFlat'),     value: 'FLAT' },
    { label: this.translate.instant('grantTicket.interestTypeReducing'), value: 'REDUCING' }
  ]);

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private authService: AuthService,
    private customerService: CustomerService,
    private ticketService: TicketService,
    private router: Router,
    private translate: TranslateService
  ) {
    this.form = this.fb.group({
      customerId:    [null as number | null, Validators.required],
      loanAmount:    [null as number | null, [Validators.required, Validators.min(1)]],
      interestRate:  [2.5,  [Validators.required, Validators.min(0)]],
      interestType:  ['FLAT', Validators.required],
      periodMonths:  [6,    [Validators.required, Validators.min(1)]],
      notes:         [''],
      goldItems:     this.fb.array([this.createGoldItemGroup()])
    });
  }

  ngOnInit(): void {
    const customerIdParam = this.route.snapshot.queryParamMap.get('customerId');
    if (customerIdParam) {
      const id = Number(customerIdParam);
      this.form.patchValue({ customerId: id });
      this.customerService.getById(id).subscribe({
        next: (c) => this.customer.set(c),
        error: () => {}
      });
    }
  }

  get goldItemsArray(): FormArray { return this.form.get('goldItems') as FormArray; }
  asFormGroup(c: unknown): FormGroup { return c as FormGroup; }

  private createGoldItemGroup(): FormGroup {
    return this.fb.group({
      description:    ['',          Validators.required],
      goldType:       ['NECKLACE',  Validators.required],
      weightGrams:    [null as number | null, [Validators.required, Validators.min(0.001)]],
      purity:         ['K22',       Validators.required],
      estimatedValue: [null as number | null]
    });
  }

  private emptyPhotoState(): ItemPhotoState {
    return { preview: null, url: null, uploading: false, error: null };
  }

  addGoldItem(): void {
    this.goldItemsArray.push(this.createGoldItemGroup());
    this.itemPhotos.push(this.emptyPhotoState());
  }

  removeGoldItem(index: number): void {
    if (this.goldItemsArray.length > 1) {
      this.goldItemsArray.removeAt(index);
      this.itemPhotos.splice(index, 1);
    }
  }

  // ── Gold item photo ──────────────────────────────────────────────────────────

  onGoldPhotoSelected(event: Event, index: number): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    if (!['image/jpeg', 'image/jpg', 'image/png', 'image/webp'].includes(file.type)) {
      this.itemPhotos[index].error = 'Only JPEG, PNG, or WEBP allowed.';
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.itemPhotos[index].error = 'File must be under 5 MB.';
      return;
    }

    this.itemPhotos[index].error = null;

    // Preview
    const reader = new FileReader();
    reader.onload = (e) => this.itemPhotos[index].preview = e.target?.result as string;
    reader.readAsDataURL(file);

    // Upload
    this.itemPhotos[index].uploading = true;
    this.customerService.uploadGoldPhoto(file).subscribe({
      next: (res) => {
        this.itemPhotos[index].url      = res.url;
        this.itemPhotos[index].uploading = false;
      },
      error: () => {
        this.itemPhotos[index].error    = 'Upload failed. Try again.';
        this.itemPhotos[index].uploading = false;
      }
    });
  }

  removeGoldPhoto(index: number): void {
    this.itemPhotos[index] = this.emptyPhotoState();
  }

  get anyPhotoUploading(): boolean {
    return this.itemPhotos.some(p => p.uploading);
  }

  // ── Submit ───────────────────────────────────────────────────────────────────

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    const shopId = this.authService.currentUser()?.shopId;
    if (!shopId) { this.errorMessage.set('No shop linked to this account.'); return; }

    if (this.anyPhotoUploading) {
      this.errorMessage.set('Photos are still uploading. Please wait.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    const raw = this.form.getRawValue();

    this.ticketService.grantTicket(shopId, {
      customerId:   raw.customerId,
      loanAmount:   raw.loanAmount,
      interestRate: raw.interestRate,
      interestType: raw.interestType,
      periodMonths: raw.periodMonths,
      notes:        raw.notes || undefined,
      goldItems:    raw.goldItems.map((item: any, i: number) => ({
        description:    item.description,
        goldType:       item.goldType,
        weightGrams:    item.weightGrams,
        purity:         item.purity,
        estimatedValue: item.estimatedValue ?? undefined,
        photoUrl:       this.itemPhotos[i]?.url ?? undefined
      }))
    }).subscribe({
      next: (ticket) => { this.loading.set(false); this.createdTicket.set(ticket); },
      error: (err)   => { this.loading.set(false); this.errorMessage.set(err?.error?.message || 'Could not grant ticket.'); }
    });
  }

  goToTicket(): void {
    const ticket = this.createdTicket();
    if (ticket) this.router.navigate(['/shop/tickets', ticket.id]);
  }
}