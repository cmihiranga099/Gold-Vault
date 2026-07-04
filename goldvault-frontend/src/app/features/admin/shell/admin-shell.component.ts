import { Component, computed } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api';
import { TopnavComponent } from '../../../shared/components/topnav/topnav.component';
import { SidebarComponent } from '../../../shared/components/sidebar';
import { LanguageService } from '../../../core/services/language.service';

@Component({
  selector: 'app-admin-shell',
  standalone: true,
  imports: [RouterOutlet, TopnavComponent, SidebarComponent],
  templateUrl: './admin-shell.component.html',
  styleUrl: './admin-shell.component.scss'
})
export class AdminShellComponent {
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
          { label: t('sidebar.dashboard'), icon: 'pi pi-th-large', routerLink: '/admin/dashboard' }
        ]
      },
      {
        label: t('sidebar.compliance'),
        icon: 'pi pi-shield',
        items: [
          { label: t('sidebar.amlMonitoring'), icon: 'pi pi-shield', routerLink: '/admin/aml' },
          { label: t('sidebar.licenseVerification'), icon: 'pi pi-verified', routerLink: '/admin/licenses' }
        ]
      },
      {
        label: t('sidebar.management'),
        icon: 'pi pi-building',
        items: [
          { label: t('sidebar.shops'), icon: 'pi pi-building', routerLink: '/admin/shops' },
          { label: t('sidebar.reports'), icon: 'pi pi-chart-bar', routerLink: '/admin/reports' }
        ]
      }
    ];
  });
}