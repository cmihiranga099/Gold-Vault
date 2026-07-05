import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LanguageService } from './core/services/language.service';
import { ThemeService } from './core/services/theme.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet />`
})
export class AppComponent implements OnInit {
  constructor(
    private languageService: LanguageService,
    private themeService: ThemeService
  ) {}

  ngOnInit(): void {
    this.languageService.init();
    this.themeService.init();
  }
}