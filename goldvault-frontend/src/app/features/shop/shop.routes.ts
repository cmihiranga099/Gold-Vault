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
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  }
];