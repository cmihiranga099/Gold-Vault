import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../core/auth/auth.service';
import { TicketService, CsvImportSummary } from '../../../core/services/ticket.service';

@Component({
  selector: 'app-bulk-import',
  standalone: true,
  imports: [CommonModule, RouterLink, ButtonModule, TagModule, ProgressSpinnerModule, TranslatePipe],
  templateUrl: './bulk-import.component.html',
  styleUrl:    './bulk-import.component.scss'
})
export class BulkImportComponent {
  selectedFile = signal<File | null>(null);
  fileName     = signal<string | null>(null);

  uploading = signal(false);
  uploadError = signal<string | null>(null);
  summary   = signal<CsvImportSummary | null>(null);

  templateDownloading = signal(false);

  constructor(
    private authService:  AuthService,
    private ticketService: TicketService,
    private translate:     TranslateService
  ) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    if (!file.name.toLowerCase().endsWith('.csv')) {
      this.uploadError.set(this.translate.instant('bulkImport.errNotCsv'));
      return;
    }

    this.uploadError.set(null);
    this.summary.set(null);
    this.selectedFile.set(file);
    this.fileName.set(file.name);
  }

  removeFile(): void {
    this.selectedFile.set(null);
    this.fileName.set(null);
    this.summary.set(null);
    this.uploadError.set(null);
  }

  downloadTemplate(): void {
    this.templateDownloading.set(true);
    this.ticketService.downloadCsvTemplate().subscribe({
      next: (blob) => {
        this.templateDownloading.set(false);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'goldvault-ticket-import-template.csv';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.templateDownloading.set(false)
    });
  }

  upload(): void {
    const file = this.selectedFile();
    const shopId = this.authService.currentUser()?.shopId;

    if (!file) { this.uploadError.set(this.translate.instant('bulkImport.errSelectFirst')); return; }
    if (!shopId) { this.uploadError.set(this.translate.instant('bulkImport.errNoShop')); return; }

    this.uploading.set(true);
    this.uploadError.set(null);
    this.summary.set(null);

    this.ticketService.bulkImportCsv(shopId, file).subscribe({
      next: (res) => {
        this.uploading.set(false);
        this.summary.set(res.data);
      },
      error: (err) => {
        this.uploading.set(false);
        this.uploadError.set(err?.error?.message || this.translate.instant('bulkImport.errImportFailed'));
      }
    });
  }

  successRows(): number {
    return this.summary()?.results.filter(r => r.success).length ?? 0;
  }

  failedRows(): number {
    return this.summary()?.results.filter(r => !r.success).length ?? 0;
  }
}