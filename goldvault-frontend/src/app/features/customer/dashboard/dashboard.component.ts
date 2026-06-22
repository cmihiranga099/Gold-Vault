import { Component } from '@angular/core';

@Component({
  selector: 'app-customer-dashboard',
  standalone: true,
  template: `
    <div style="padding: 2rem;">
      <h1>Customer Dashboard</h1>
      <p>Coming next — active tickets, loan balance, payment history.</p>
    </div>
  `
})
export class DashboardComponent {}