import { Component, computed, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { TopbarComponent } from '../../../shared/components/topbar/topbar.component';
import { LanguageService } from '../../../core/services/language.service';

@Component({
  selector: 'app-customer-shell',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, TopbarComponent],
  templateUrl: './customer-shell.component.html',
  styleUrl: './customer-shell.component.scss'
})
export class CustomerShellComponent {
  sidebarOpen = signal(true);

  constructor(
    private translate: TranslateService,
    private lang: LanguageService
  ) {}

  toggleSidebar(): void {
    this.sidebarOpen.set(!this.sidebarOpen());
  }

  menuItems = computed<MenuItem[]>(() => {
    this.lang.currentLang(); // recompute labels on language change
    const t = (key: string) => this.translate.instant(key);

    return [
      {
        label: t('sidebar.overview'),
        icon: 'pi pi-home',
        expanded: true,
        items: [
          { label: t('sidebar.dashboard'), icon: 'pi pi-th-large', routerLink: '/customer/dashboard' }
        ]
      },
      {
        label: t('sidebar.marketplace'),
        icon: 'pi pi-shop',
        items: [
          { label: t('sidebar.browseMarketplace'), icon: 'pi pi-shop', routerLink: '/customer/marketplace' },
          { label: t('sidebar.findShops'), icon: 'pi pi-map-marker', routerLink: '/customer/find-shops' }
        ]
      },
      {
        label: t('sidebar.tools'),
        icon: 'pi pi-calculator',
        items: [
          { label: t('sidebar.calculator'), icon: 'pi pi-calculator', routerLink: '/customer/calculator' },
          { label: t('sidebar.liveGoldRates'), icon: 'pi pi-chart-line', routerLink: '/gold-rates' }
        ]
      }
    ];
  });
}