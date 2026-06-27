import { Injectable, signal } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

export type AppLang = 'en' | 'si' | 'ta';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private readonly STORAGE_KEY = 'gv_lang';

  currentLang = signal<AppLang>('en');

  constructor(private translate: TranslateService) {}

  init(): void {
    const saved = localStorage.getItem(this.STORAGE_KEY) as AppLang | null;
    const lang: AppLang = saved ?? 'en';
    this.translate.addLangs(['en', 'si', 'ta']);
    this.translate.setFallbackLang('en');
    this.setLang(lang);
  }

  setLang(lang: AppLang): void {
    this.translate.use(lang);
    this.currentLang.set(lang);
    localStorage.setItem(this.STORAGE_KEY, lang);
    document.documentElement.lang = lang;
  }
}