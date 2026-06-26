import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'auth',
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
      }
    ]
  },
  {
    path: 'customer',
    canActivate: [authGuard],
    data: { roles: ['ROLE_CUSTOMER'] },
    loadChildren: () => import('./features/customer/customer.routes').then(m => m.CUSTOMER_ROUTES)
  },
  {
    path: 'shop',
    canActivate: [authGuard],
    data: { roles: ['ROLE_SHOP_ADMIN', 'ROLE_STAFF'] },
    loadChildren: () => import('./features/shop/shop.routes').then(m => m.SHOP_ROUTES)
  },
  {
    path: 'admin',
    canActivate: [authGuard],
    data: { roles: ['ROLE_ADMIN'] },
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  },
  {
    path: '',
    redirectTo: 'auth/login',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'auth/login'
  }
];