import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { SessionStore } from './session.store';
import { UserResponse } from '../api/contracts/auth';
import { Router } from '@angular/router';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let sessionStore: SessionStore;
  let router: Router;

  const mockUser: UserResponse = {
    id: 'u1',
    email: 'test@bookhub.local',
    displayName: 'Tester',
    role: 'ROLE_USER',
    createdAt: '2026-07-23T20:00:00Z'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        SessionStore,
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting()
      ]
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    sessionStore = TestBed.inject(SessionStore);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should attach Bearer token to protected request when authenticated', () => {
    sessionStore.setSession('token-abc', mockUser);

    http.get('/api/v1/library/me/books').subscribe();

    const req = httpMock.expectOne('/api/v1/library/me/books');
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-abc');
  });

  it('should not attach Bearer token to login request', () => {
    sessionStore.setSession('token-abc', mockUser);

    http.post('/api/v1/auth/login', { email: 'a', password: 'b' }).subscribe();

    const req = httpMock.expectOne('/api/v1/auth/login');
    expect(req.request.headers.has('Authorization')).toBe(false);
  });
});
