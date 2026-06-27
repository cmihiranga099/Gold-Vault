import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { definePreset } from '@primeuix/themes';
import { provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';

import { routes } from './app.routes';
import { jwtInterceptor } from './core/auth/jwt.interceptor';
import { errorInterceptor } from './core/auth/error.interceptor';

const GoldVaultPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '#fbf6ec',
      100: '#f3e6c4',
      200: '#e9d49c',
      300: '#ddc285',
      400: '#d4b066',
      500: '#c9a14a',
      600: '#a8822f',
      700: '#8a6a26',
      800: '#6c521e',
      900: '#4f3b15',
      950: '#332610'
    },
    colorScheme: {
      light: {
        primary: {
          color: '#c9a14a',
          contrastColor: '#1c2b33',
          hoverColor: '#a8822f',
          activeColor: '#8a6a26'
        }
      }
    }
  }
});

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([jwtInterceptor, errorInterceptor])),
    provideAnimationsAsync(),
    providePrimeNG({
      theme: {
        preset: GoldVaultPreset,
        options: { darkModeSelector: false }
      }
    }),
    provideTranslateService({
      loader: provideTranslateHttpLoader({
        prefix: './assets/i18n/',
        suffix: '.json'
      }),
      fallbackLang: 'en',
      lang: 'en'
    })
  ]
};