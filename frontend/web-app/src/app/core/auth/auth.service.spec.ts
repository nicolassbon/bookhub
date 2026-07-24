import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { SessionStore } from './session.store';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let sessionStore: SessionStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        SessionStore,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    sessionStore = TestBed.inject(SessionStore);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should send login request and store session', () => {
    service.login('test@bookhub.local', 'password123').subscribe(response => {
      expect(response.accessToken).toBe('jwt-token');
      expect(sessionStore.isAuthenticated()).toBe(true);
    });

    const req = httpMock.expectOne('/api/v1/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);

    req.flush({
      accessToken: 'jwt-token',
      tokenType: 'Bearer',
      expiresInSeconds: 3600,
      user: {
        id: 'u1',
        email: 'test@bookhub.local',
        displayName: 'Tester',
        role: 'ROLE_USER',
        createdAt: '2026-07-23T20:00:00Z'
      }
    });
  });

  it('should send refresh request with credentials', () => {
    service.refreshToken().subscribe(res => {
      expect(res.accessToken).toBe('new-jwt-token');
    });

    const req = httpMock.expectOne('/api/v1/auth/refresh');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);

    req.flush({
      accessToken: 'new-jwt-token',
      tokenType: 'Bearer',
      expiresInSeconds: 3600
    });
  });
});
