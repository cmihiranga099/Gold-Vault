import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../core/auth/auth.service';
import { LanguageService, AppLang } from '../../../core/services/language.service';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, ButtonModule, TooltipModule],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss'
})
export class TopbarComponent {
  @Output() menuToggle = new EventEmitter<void>();

  private readonly langOrder: AppLang[] = ['en', 'si', 'ta'];

  constructor(
    public authService: AuthService,
    public themeService: ThemeService,
    public langService: LanguageService,
    private translate: TranslateService
  ) {}

  cycleLang(): void {
    const idx = this.langOrder.indexOf(this.langService.currentLang());
    const next = this.langOrder[(idx + 1) % this.langOrder.length];
    this.langService.setLang(next);
  }

  logout(): void {
    this.authService.logout();
  }

  langTooltip(): string {
    return this.translate.instant('topbar.switchLanguage');
  }

  themeTooltip(): string {
    return this.translate.instant(this.themeService.isDark() ? 'topbar.lightMode' : 'topbar.darkMode');
  }

  accountTooltip(): string {
    return this.authService.currentUser()?.fullName ?? this.translate.instant('topbar.account');
  }

  logoutTooltip(): string {
    return this.translate.instant('sidebar.signOut');
  }
}