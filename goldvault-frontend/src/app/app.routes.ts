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
    path: 'auctions',
    loadComponent: () => import('./features/public/auctions/auctions.component').then(m => m.AuctionsListComponent)
  },
  {
    path: 'auctions/:id',
    loadComponent: () => import('./features/public/auction-detail/auction-detail.component').then(m => m.AuctionDetailComponent)
  },
  {
    path: 'gold-rates',
    loadComponent: () => import('./features/public/gold-rates/gold-rates-public.component').then(m => m.GoldRatesPublicComponent)
  },
  {
    path: 'auth/shop-register',
    loadComponent: () => import('./features/auth/shop-register/shop-register.component').then(m => m.ShopRegisterComponent)
  },
  {
    path: 'shops/:id',
    loadComponent: () => import('./features/public/shop-profile/shop-profile.component').then(m => m.PublicShopProfileComponent)
  },
  {
    path: 'calculator',
    loadComponent: () => import('./features/public/calculator/calculator.component').then(m => m.InterestCalculatorComponent)
  },
  {
    path: '**',
    redirectTo: 'auth/login'
  }
];