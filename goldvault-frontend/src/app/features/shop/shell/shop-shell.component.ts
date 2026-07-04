import { Component, computed } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { LanguageService } from '../../../core/services/language.service';

@Component({
  selector: 'app-shop-shell',
  standalone: true,
  imports: [RouterOutlet, TopnavComponent, SidebarComponent],
  templateUrl: './shop-shell.component.html',
  styleUrl: './shop-shell.component.scss'
})
export class ShopShellComponent {
  constructor(private translate: TranslateService, private lang: LanguageService) {}

  menuItems = computed<MenuItem[]>(() => {
    this.lang.currentLang();
    const t = (key: string) => this.translate.instant(key);

    return [
      {
        label: t('sidebar.overview'),
        icon: 'pi pi-home',
        expanded: true,
        items: [
          { label: t('sidebar.dashboard'), icon: 'pi pi-th-large', routerLink: '/shop/dashboard' },
          { label: t('sidebar.analytics'), icon: 'pi pi-chart-line', routerLink: '/shop/analytics' }
        ]
      },
      {
        label: t('sidebar.customers'),
        icon: 'pi pi-users',
        items: [
          { label: t('sidebar.allCustomers'), icon: 'pi pi-users', routerLink: '/shop/customers' },
          { label: t('sidebar.addCustomer'), icon: 'pi pi-user-plus', routerLink: '/shop/customers/new' }
        ]
      },
      {
        label: t('sidebar.pawnTickets'),
        icon: 'pi pi-ticket',
        items: [
          { label: t('sidebar.newTicket'), icon: 'pi pi-plus-circle', routerLink: '/shop/tickets/new' }
        ]
      },
      {
        label: t('sidebar.marketplace'),
        icon: 'pi pi-shop',
        items: [
          { label: t('sidebar.shopMarketplace'), icon: 'pi pi-shop', routerLink: '/shop/marketplace' },
          { label: t('sidebar.bulkImport'), icon: 'pi pi-upload', routerLink: '/shop/bulk-import' },
          { label: t('sidebar.promotions'), icon: 'pi pi-megaphone', routerLink: '/shop/promotions' }
        ]
      },
      {
        label: t('sidebar.settings'),
        icon: 'pi pi-cog',
        items: [
          { label: t('sidebar.goldRates'), icon: 'pi pi-dollar', routerLink: '/shop/gold-rates' },
          { label: t('sidebar.apiKeys'), icon: 'pi pi-key', routerLink: '/shop/api-keys' }
        ]
      }
    ];
  });
}