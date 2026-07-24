import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { authGuard, guestGuard } from './auth.guard';
import { SessionStore } from './session.store';
import { UserResponse } from '../api/contracts/auth';

describe('Auth Guards', () => {
  let sessionStore: SessionStore;
  let router: Router;

  const mockUser: UserResponse = {
    id: 'u1',
    email: 'test@bookhub.local',
    displayName: 'Tester',
    role: 'ROLE_USER',
    createdAt: '2026-07-23T20:00:00Z'
  };

  const dummyRoute = {} as ActivatedRouteSnapshot;
  const dummyState = (url: string) => ({ url } as RouterStateSnapshot);

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SessionStore]
    });

    sessionStore = TestBed.inject(SessionStore);
    router = TestBed.inject(Router);
  });

  describe('authGuard', () => {
    it('should allow access when authenticated', () => {
      sessionStore.setSession('token', mockUser);
      const result = TestBed.runInInjectionContext(() => authGuard(dummyRoute, dummyState('/library')));
      expect(result).toBe(true);
    });

    it('should redirect to /login with returnUrl when unauthenticated', () => {
      sessionStore.clearSession();
      const result = TestBed.runInInjectionContext(() => authGuard(dummyRoute, dummyState('/library'))) as UrlTree;
      expect(result instanceof UrlTree).toBe(true);
      expect(router.serializeUrl(result)).toContain('/login');
    });
  });

  describe('guestGuard', () => {
    it('should allow access to guests when unauthenticated', () => {
      sessionStore.clearSession();
      const result = TestBed.runInInjectionContext(() => guestGuard(dummyRoute, dummyState('/login')));
      expect(result).toBe(true);
    });

    it('should redirect to / when authenticated', () => {
      sessionStore.setSession('token', mockUser);
      const result = TestBed.runInInjectionContext(() => guestGuard(dummyRoute, dummyState('/login'))) as UrlTree;
      expect(result instanceof UrlTree).toBe(true);
      expect(router.serializeUrl(result)).toBe('/');
    });
  });
});
