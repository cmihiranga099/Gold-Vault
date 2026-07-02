import { Routes } from '@angular/router';

export const CUSTOMER_ROUTES: Routes = [
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'tickets/:id',
    loadComponent: () => import('./ticket-detail/ticket-detail.component').then(m => m.CustomerTicketDetailComponent)
  },
  {
    path: 'marketplace',
    loadComponent: () => import('./marketplace/marketplace.component').then(m => m.CustomerMarketplaceComponent)
  },
  {
    path: 'listings/:id',
    loadComponent: () => import('./listing-detail/listing-detail.component').then(m => m.ListingDetailComponent)
  },
  {
    path: 'find-shops',
    loadComponent: () => import('./shop-finder/shop-finder.component').then(m => m.ShopFinderComponent)
  },
  {
    path: 'calculator',
    loadComponent: () => import('../public/calculator/calculator.component').then(m => m.InterestCalculatorComponent)
  },
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  }
];