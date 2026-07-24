import { inject, provideAppInitializer } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';
import { SessionStore } from './session.store';

export function initializeAppAuth() {
  const authService = inject(AuthService);
  const sessionStore = inject(SessionStore);

  return async () => {
    try {
      const refreshRes = await firstValueFrom(authService.refreshToken());
      if (refreshRes?.accessToken) {
        await firstValueFrom(authService.getCurrentUser());
      }
    } catch {
      // Anonymous guest or no valid refresh cookie present
      sessionStore.clearSession();
    } finally {
      sessionStore.markInitialized();
    }
  };
}

export const AUTH_INITIALIZER_PROVIDER = provideAppInitializer(initializeAppAuth());
