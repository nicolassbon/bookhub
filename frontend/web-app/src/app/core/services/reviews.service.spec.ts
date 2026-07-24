import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ReviewsService } from './reviews.service';

describe('ReviewsService', () => {
  let service: ReviewsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ReviewsService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(ReviewsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch public reviews for book', () => {
    service.getReviewsForBook('b1').subscribe(list => {
      expect(list.length).toBe(1);
    });

    const req = httpMock.expectOne('/api/v1/books/b1/reviews');
    expect(req.request.method).toBe('GET');

    req.flush([
      { id: 'r1', userId: 'u1', bookId: 'b1', rating: 5, content: 'Increíble', status: 'VISIBLE', createdAt: '2026-07-23T20:00:00Z' }
    ]);
  });
});
