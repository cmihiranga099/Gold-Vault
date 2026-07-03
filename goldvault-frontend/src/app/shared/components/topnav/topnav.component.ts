import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../../core/auth/auth.service';
import { LangSelectorComponent } from '../lang-selector/lang-selector.component';
import { CurrencySwitcherComponent } from '../currency-switcher/currency-switcher.component';


@Component({
  selector: 'app-topnav',
  standalone: true,
  imports: [CommonModule, ButtonModule, TooltipModule, TranslatePipe, LangSelectorComponent,CurrencySwitcherComponent],
  templateUrl: './topnav.component.html',
  styleUrl: './topnav.component.scss'
})
export class TopnavComponent {
  constructor(public authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
}