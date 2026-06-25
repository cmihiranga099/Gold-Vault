import { Routes } from '@angular/router';

export const SHOP_ROUTES: Routes = [
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard.component').then(m => m.ShopDashboardComponent)
  },
  {
    path: 'customers',
    loadComponent: () => import('./customers/customers.component').then(m => m.CustomersComponent)
  },
  {
    path: 'customers/new',
    loadComponent: () => import('./customer-form/customer-form.component').then(m => m.CustomerFormComponent)
  },
  {
    path: 'customers/:id',
    loadComponent: () => import('./customer-detail/customer-detail.component').then(m => m.CustomerDetailComponent)
  },
  {
    path: 'tickets/new',
    loadComponent: () => import('./grant-ticket/grant-ticket.component').then(m => m.GrantTicketComponent)
  },
  {
    path: 'tickets/:id',
    loadComponent: () => import('./ticket-detail/ticket-detail.component').then(m => m.ShopTicketDetailComponent)
  },
  {
    path: 'gold-rates',
    loadComponent: () => import('./gold-rates/gold-rates.component').then(m => m.GoldRatesComponent)
  },
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  }
];