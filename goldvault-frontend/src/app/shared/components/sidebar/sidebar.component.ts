import { Component, HostBinding, Input, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PanelMenuModule } from 'primeng/panelmenu';
import { ButtonModule } from 'primeng/button';
import { MenuItem } from 'primeng/api';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../core/auth/auth.service';
import { LanguageService, AppLang } from '../../../core/services/language.service';
import { CurrencyService, CURRENCY_OPTIONS, Currency } from '../../../core/services/currency.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, PanelMenuModule, ButtonModule, TranslatePipe],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  /** Portal-specific navigation items (Dashboard, Customers, etc.) */
  @Input({ required: true }) items: MenuItem[] = [];

  /** Controls visibility on narrow viewports (toggled by the topbar hamburger). Always visible on desktop widths. */
  @Input() open = true;

  @HostBinding('class.gv-sidebar-collapsed')
  get isCollapsed(): boolean {
    return !this.open;
  }

  allExpanded = signal(false);

  constructor(
    public authService: AuthService,
    private translate: TranslateService,
    private langService: LanguageService,
    private currencyService: CurrencyService
  ) {}

  /** Portal items + universal language/currency/sign-out items appended. */
  fullItems = computed<MenuItem[]>(() => {
    const currentLang = this.langService.currentLang(); // dependency, recompute on change
    this.currencyService.selectedCurrency();              // dependency
    const t = (key: string) => this.translate.instant(key);

    const languageGroup: MenuItem = {
      label: t('sidebar.language'),
      icon: 'pi pi-globe',
      items: [
        { label: 'English', icon: currentLang === 'en' ? 'pi pi-check' : 'pi pi-circle-off', command: () => this.langService.setLang('en') },
        { label: 'සිංහල',   icon: currentLang === 'si' ? 'pi pi-check' : 'pi pi-circle-off', command: () => this.langService.setLang('si') },
        { label: 'தமிழ்',    icon: currentLang === 'ta' ? 'pi pi-check' : 'pi pi-circle-off', command: () => this.langService.setLang('ta') }
      ]
    };

    const currentCurrency = this.currencyService.selectedCurrency();
    const currencyGroup: MenuItem = {
      label: t('sidebar.currency'),
      icon: 'pi pi-wallet',
      items: CURRENCY_OPTIONS.map(option => ({
        label: `${option.flag} ${option.code} — ${option.label}`,
        icon: currentCurrency === option.code ? 'pi pi-check' : 'pi pi-circle-off',
        command: () => this.currencyService.setCurrency(option.code)
      }))
    };

    const signOutItem: MenuItem = {
      label: t('sidebar.signOut'),
      icon: 'pi pi-sign-out',
      command: () => this.authService.logout()
    };

    return [...this.items, languageGroup, currencyGroup, signOutItem];
  });

  toggleAll(): void {
    const next = !this.allExpanded();
    this.allExpanded.set(next);
    this.items.forEach(item => this.setExpandedRecursive(item, next));
    this.items = [...this.items];
  }

  private setExpandedRecursive(item: MenuItem, expanded: boolean): void {
    item.expanded = expanded;
    item.items?.forEach(child => this.setExpandedRecursive(child, expanded));
  }
}