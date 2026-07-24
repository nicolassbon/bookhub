import { render, screen } from '@testing-library/angular';
import { TestBed } from '@angular/core/testing';
import { BookReviewsComponent } from './book-reviews';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

describe('BookReviewsComponent', () => {
  it('should render reviews section header and list reviews', async () => {
    const { fixture } = await render(BookReviewsComponent, {
      componentInputs: { bookId: 'b1' },
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    const httpMock = TestBed.inject(HttpTestingController);
    const req = httpMock.expectOne('/api/v1/books/b1/reviews');
    req.flush([
      { id: 'r1', userId: 'u1', userDisplayName: 'Nico', bookId: 'b1', rating: 5, content: 'Excelente libro', status: 'VISIBLE', createdAt: '2026-07-23T20:00:00Z' }
    ]);

    fixture.detectChanges();

    expect(screen.getByText('Reseñas de Lectores')).toBeTruthy();
    expect(screen.getByText('Excelente libro')).toBeTruthy();
  });
});
