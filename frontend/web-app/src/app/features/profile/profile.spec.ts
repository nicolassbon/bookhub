import { render, screen } from '@testing-library/angular';
import { TestBed } from '@angular/core/testing';
import { ProfileComponent } from './profile';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

describe('ProfileComponent', () => {
  it('should render profile header and form', async () => {
    const { fixture } = await render(ProfileComponent, {
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    const httpMock = TestBed.inject(HttpTestingController);
    const req = httpMock.expectOne('/api/v1/users/me');
    req.flush({
      id: 'u1',
      email: 'nico@example.com',
      displayName: 'Nicolas'
    });

    fixture.detectChanges();

    expect(screen.getByText('Mi Perfil de Lector')).toBeTruthy();
    expect(screen.getByText('Nicolas')).toBeTruthy();
    expect(screen.getByText('nico@example.com')).toBeTruthy();
  });
});
