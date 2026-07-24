import { Injectable, computed, signal } from '@angular/core';
import { UserResponse } from '../api/contracts/auth';

@Injectable({
  providedIn: 'root'
})
export class SessionStore {
  readonly accessToken = signal<string | null>(null);
  readonly user = signal<UserResponse | null>(null);
  readonly isInitialized = signal<boolean>(false);

  readonly isAuthenticated = computed(() => this.accessToken() !== null);

  setSession(token: string, user: UserResponse) {
    this.accessToken.set(token);
    this.user.set(user);
    this.isInitialized.set(true);
  }

  setAccessToken(token: string) {
    this.accessToken.set(token);
  }

  setUser(user: UserResponse) {
    this.user.set(user);
  }

  clearSession() {
    this.accessToken.set(null);
    this.user.set(null);
    this.isInitialized.set(true);
  }

  markInitialized() {
    this.isInitialized.set(true);
  }
}
