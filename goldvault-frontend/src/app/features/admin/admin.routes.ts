import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard.component').then(m => m.AdminDashboardComponent)
  },
  {
    path: 'shops',
    loadComponent: () => import('./shops/shops.component').then(m => m.AdminShopsComponent)
  },
  {
    path: 'reports',
    loadComponent: () => import('./reports/reports.component').then(m => m.AdminReportsComponent)
  },
  {
    path: 'aml',
    loadComponent: () => import('./aml/aml.component').then(m => m.AmlDashboardComponent)
  },
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  }
];