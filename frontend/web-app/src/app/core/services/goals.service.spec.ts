import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { GoalsService } from './goals.service';

describe('GoalsService', () => {
  let service: GoalsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        GoalsService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(GoalsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch yearly reading goal', () => {
    service.getYearlyGoal().subscribe(goal => {
      expect(goal.targetBooks).toBe(12);
    });

    const req = httpMock.expectOne('/api/v1/goals/yearly');
    expect(req.request.method).toBe('GET');

    req.flush({
      id: 'g1',
      userId: 'u1',
      year: 2026,
      targetBooks: 12,
      booksReadCount: 3,
      percentageAchieved: 25,
      isAchieved: false
    });
  });
});
