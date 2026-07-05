import { Injectable, signal } from '@angular/core';

export type AppTheme = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly STORAGE_KEY = 'gv_theme';

  isDark = signal<boolean>(false);

  init(): void {
    const saved = localStorage.getItem(this.STORAGE_KEY) as AppTheme | null;
    const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)').matches;
    const theme: AppTheme = saved ?? (prefersDark ? 'dark' : 'light');
    this.apply(theme);
  }

  toggle(): void {
    this.apply(this.isDark() ? 'light' : 'dark');
  }

  private apply(theme: AppTheme): void {
    document.documentElement.setAttribute('data-theme', theme);
    this.isDark.set(theme === 'dark');
    localStorage.setItem(this.STORAGE_KEY, theme);
  }
}