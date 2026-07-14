import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.page').then(m => m.LoginPage)
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register.page').then(m => m.RegisterPage)
  },
  {
    path: 'tabs',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/tabs/tabs.page').then(m => m.TabsPage),
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard.page').then(m => m.DashboardPage)
      },
      {
        path: 'marketplace',
        loadComponent: () => import('./pages/marketplace/marketplace.page').then(m => m.MarketplacePage)
      },
      {
        path: 'notifications',
        loadComponent: () => import('./pages/notifications/notifications.page').then(m => m.NotificationsPage)
      },
      {
        path: 'account',
        loadComponent: () => import('./pages/account/account.page').then(m => m.AccountPage)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  {
    path: 'tickets/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/ticket-detail/ticket-detail.page').then(m => m.TicketDetailPage)
  },
  {
    path: 'create-listing',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/create-listing/create-listing.page').then(m => m.CreateListingPage)
  },
  {
    path: 'listings/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/listing-detail/listing-detail.page').then(m => m.ListingDetailPage)
  },
  {
    path: 'shop-finder',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/shop-finder/shop-finder.page').then(m => m.ShopFinderPage)
  },
  {
    path: 'calculator',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/calculator/calculator.page').then(m => m.CalculatorPage)
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];