import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError, Observable, BehaviorSubject, filter, take } from 'rxjs';
import { SessionStore } from './session.store';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';

let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

const PUBLIC_PATHS = [
  '/api/v1/auth/login',
  '/api/v1/auth/register',
  '/api/v1/auth/refresh',
  '/api/v1/auth/forgot-password',
  '/api/v1/auth/reset-password'
];

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const sessionStore = inject(SessionStore);
  const authService = inject(AuthService);
  const router = inject(Router);

  const isPublicPath = PUBLIC_PATHS.some(path => req.url.includes(path));

  let authReq = req;
  const token = sessionStore.accessToken();

  if (token && !isPublicPath) {
    authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isPublicPath) {
        return handle401Error(authReq, next, sessionStore, authService, router);
      }
      return throwError(() => error);
    })
  );
};

function handle401Error(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
  sessionStore: SessionStore,
  authService: AuthService,
  router: Router
): Observable<any> {
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    return authService.refreshToken().pipe(
      switchMap(res => {
        isRefreshing = false;
        refreshTokenSubject.next(res.accessToken);
        return next(
          req.clone({
            headers: req.headers.set('Authorization', `Bearer ${res.accessToken}`)
          })
        );
      }),
      catchError(refreshError => {
        isRefreshing = false;
        sessionStore.clearSession();
        router.navigate(['/login']);
        return throwError(() => refreshError);
      })
    );
  } else {
    return refreshTokenSubject.pipe(
      filter(token => token !== null),
      take(1),
      switchMap(token =>
        next(
          req.clone({
            headers: req.headers.set('Authorization', `Bearer ${token!}`)
          })
        )
      )
    );
  }
}
