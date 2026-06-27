import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LanguageService, AppLang } from '../../../core/services/language.service';

@Component({
  selector: 'app-lang-selector',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="lang-selector">
      <button
        class="lang-btn"
        [class.lang-btn-active]="langService.currentLang() === 'en'"
        (click)="set('en')"
      >EN</button>
      <span class="lang-divider">|</span>
      <button
        class="lang-btn"
        [class.lang-btn-active]="langService.currentLang() === 'si'"
        (click)="set('si')"
      >සිං</button>
      <span class="lang-divider">|</span>
      <button
        class="lang-btn"
        [class.lang-btn-active]="langService.currentLang() === 'ta'"
        (click)="set('ta')"
      >தமி</button>
    </div>
  `,
  styles: [`
    .lang-selector {
      display: flex;
      align-items: center;
      gap: 2px;
    }
    .lang-btn {
      background: none;
      border: none;
      cursor: pointer;
      font-size: 12px;
      font-family: inherit;
      color: var(--gv-topnav-muted, #888);
      padding: 2px 5px;
      border-radius: 4px;
      transition: color 0.15s, background 0.15s;
    }
    .lang-btn:hover {
      color: #C9A14A;
      background: rgba(201,161,74,0.08);
    }
    .lang-btn-active {
      color: #C9A14A !important;
      font-weight: 600;
    }
    .lang-divider {
      color: #ccc;
      font-size: 11px;
      user-select: none;
    }
  `]
})
export class LangSelectorComponent {
  constructor(public langService: LanguageService) {}

  set(lang: AppLang): void {
    this.langService.setLang(lang);
  }
}