import { render, screen } from '@testing-library/angular';
import { TestBed } from '@angular/core/testing';
import { GoalsWidgetComponent } from './goals-widget';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

describe('GoalsWidgetComponent', () => {
  it('should render yearly goal progress', async () => {
    const { fixture } = await render(GoalsWidgetComponent, {
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    const httpMock = TestBed.inject(HttpTestingController);
    const req = httpMock.expectOne('/api/v1/goals/yearly');
    req.flush({
      id: 'g1',
      userId: 'u1',
      year: 2026,
      targetBooks: 12,
      booksReadCount: 6,
      percentageAchieved: 50.0,
      isAchieved: false
    });

    fixture.detectChanges();

    expect(screen.getByText(/Meta de Lectura 2026/i)).toBeTruthy();
    expect(screen.getByText('50%')).toBeTruthy();
  });
});
