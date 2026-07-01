import { Routes } from '@angular/router';

export const SHOP_ROUTES: Routes = [
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard.component').then(m => m.ShopDashboardComponent)
  },
  {
    path: 'analytics',
    loadComponent: () => import('./analytics/analytics.component').then(m => m.ShopAnalyticsComponent)
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
    path: 'marketplace',
    loadComponent: () => import('./marketplace/marketplace.component').then(m => m.ShopMarketplaceComponent)
  },
  {
    path: 'bulk-import',
    loadComponent: () => import('./bulk-import/bulk-import.component').then(m => m.BulkImportComponent)
  },
  {
    path: 'promotions',
    loadComponent: () => import('./promotions/promotions.component').then(m => m.ShopPromotionsComponent)
  },
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  }
];