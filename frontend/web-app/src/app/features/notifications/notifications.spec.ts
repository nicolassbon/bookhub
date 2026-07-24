import { render, screen } from '@testing-library/angular';
import { TestBed } from '@angular/core/testing';
import { NotificationsComponent } from './notifications';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

describe('NotificationsComponent', () => {
  it('should render notifications header and empty state when empty', async () => {
    const { fixture } = await render(NotificationsComponent, {
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    const httpMock = TestBed.inject(HttpTestingController);
    const req = httpMock.expectOne('/api/v1/notifications');
    req.flush([]);

    fixture.detectChanges();

    expect(screen.getByText('Notificaciones')).toBeTruthy();
    expect(screen.getByText('Sin notificaciones')).toBeTruthy();
  });
});
