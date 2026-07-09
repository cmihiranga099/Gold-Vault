import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { HttpResponse } from '@angular/common/http';
import { DatePickerModule } from 'primeng/datepicker';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslatePipe } from '@ngx-translate/core';
import { AdminService } from '../../../core/services/admin.service';
import { RevenueReportResponse } from '../../../core/models/admin.model';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DatePickerModule, ButtonModule, ProgressSpinnerModule, TranslatePipe],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss'
})
export class AdminReportsComponent implements OnInit {
  report = signal<RevenueReportResponse | null>(null);
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  exportingPdf = signal(false);
  exportingExcel = signal(false);
  exportError = signal<string | null>(null);

  form: ReturnType<FormBuilder['group']>;

  constructor(
    private fb: FormBuilder,
    private adminService: AdminService
  ) {
    const today = new Date();
    const firstOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);

    this.form = this.fb.group({
      startDate: [firstOfMonth],
      endDate: [today]
    });
  }

  ngOnInit(): void {
    this.runReport();
  }

  runReport(): void {
    const raw = this.form.getRawValue();
    const startDate = this.toIsoDate(raw.startDate);
    const endDate = this.toIsoDate(raw.endDate);

    this.loading.set(true);
    this.errorMessage.set(null);

    this.adminService.getRevenueReport(startDate, endDate).subscribe({
      next: (report) => {
        this.report.set(report);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not generate report.');
        this.loading.set(false);
      }
    });
  }

  exportPdf(): void {
    const { startDate, endDate } = this.currentRange();
    this.exportError.set(null);
    this.exportingPdf.set(true);

    this.adminService.downloadRevenueReportPdf(startDate, endDate).subscribe({
      next: (response) => this.saveBlob(response, `revenue-report-${startDate}-to-${endDate}.pdf`,
        () => this.exportingPdf.set(false)),
      error: () => {
        this.exportingPdf.set(false);
        this.exportError.set('Could not generate PDF. Please try again.');
      }
    });
  }

  exportExcel(): void {
    const { startDate, endDate } = this.currentRange();
    this.exportError.set(null);
    this.exportingExcel.set(true);

    this.adminService.downloadRevenueReportExcel(startDate, endDate).subscribe({
      next: (response) => this.saveBlob(response, `revenue-report-${startDate}-to-${endDate}.xlsx`,
        () => this.exportingExcel.set(false)),
      error: () => {
        this.exportingExcel.set(false);
        this.exportError.set('Could not generate Excel file. Please try again.');
      }
    });
  }

  private currentRange(): { startDate: string; endDate: string } {
    const raw = this.form.getRawValue();
    return { startDate: this.toIsoDate(raw.startDate), endDate: this.toIsoDate(raw.endDate) };
  }

  private saveBlob(response: HttpResponse<Blob>, fallbackName: string, done: () => void): void {
    const blob = response.body!;
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fallbackName;
    a.click();
    window.URL.revokeObjectURL(url);
    done();
  }

  private toIsoDate(date: Date): string {
    const d = new Date(date);
    return d.toISOString().split('T')[0];
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }
}