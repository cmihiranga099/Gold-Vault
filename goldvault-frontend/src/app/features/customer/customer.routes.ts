import { Routes } from '@angular/router';

export const CUSTOMER_ROUTES: Routes = [
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'tickets/:id',
    loadComponent: () => import('./ticket-detail/ticket-detail.component').then(m => m.TicketDetailComponent)
  },
  {
    path: 'marketplace',
    loadComponent: () => import('./marketplace/marketplace.component').then(m => m.CustomerMarketplaceComponent)
  },
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  }
];