import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    router.navigate(['/auth/login']);
    return false;
  }

  const requiredRoles = route.data?.['roles'] as string[] | undefined;
  if (requiredRoles && requiredRoles.length > 0 && !authService.hasRole(...requiredRoles)) {
    router.navigate(['/auth/login']);
    return false;
  }

  return true;
};