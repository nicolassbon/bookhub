import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { 
  LoginRequest, 
  LoginResponse, 
  RegisterRequest, 
  UserResponse, 
  RefreshTokenResponse, 
  ForgotPasswordRequest, 
  ResetPasswordRequest 
} from '../api/contracts/auth';
import { SessionStore } from './session.store';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly sessionStore = inject(SessionStore);
  private readonly baseUrl = '/api/v1';

  login(email: string, password: string): Observable<LoginResponse> {
    const body: LoginRequest = { email, password };
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, body, { withCredentials: true }).pipe(
      tap(res => {
        this.sessionStore.setSession(res.accessToken, res.user);
      })
    );
  }

  register(email: string, password: string, displayName: string): Observable<UserResponse> {
    const body: RegisterRequest = { email, password, displayName };
    return this.http.post<UserResponse>(`${this.baseUrl}/auth/register`, body);
  }

  refreshToken(): Observable<RefreshTokenResponse> {
    return this.http.post<RefreshTokenResponse>(`${this.baseUrl}/auth/refresh`, {}, { withCredentials: true }).pipe(
      tap(res => {
        this.sessionStore.setAccessToken(res.accessToken);
      })
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/auth/logout`, {}, { withCredentials: true }).pipe(
      tap(() => {
        this.sessionStore.clearSession();
      })
    );
  }

  getCurrentUser(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.baseUrl}/users/me`).pipe(
      tap(user => {
        this.sessionStore.setUser(user);
      })
    );
  }

  forgotPassword(email: string): Observable<void> {
    const body: ForgotPasswordRequest = { email };
    return this.http.post<void>(`${this.baseUrl}/auth/forgot-password`, body);
  }

  resetPassword(token: string, newPassword: string): Observable<void> {
    const body: ResetPasswordRequest = { token, newPassword };
    return this.http.post<void>(`${this.baseUrl}/auth/reset-password`, body);
  }
}
