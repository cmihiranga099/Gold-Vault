import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { DatePickerModule } from 'primeng/datepicker';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { AdminService } from '../../../core/services/admin.service';
import { RevenueReportResponse } from '../../../core/models/admin.model';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DatePickerModule, ButtonModule, ProgressSpinnerModule, TopnavComponent],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss'
})
export class AdminReportsComponent implements OnInit {
  report = signal<RevenueReportResponse | null>(null);
  loading = signal(false);
  errorMessage = signal<string | null>(null);

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

  private toIsoDate(date: Date): string {
    const d = new Date(date);
    return d.toISOString().split('T')[0];
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-LK', { minimumFractionDigits: 2 }).format(amount);
  }
}