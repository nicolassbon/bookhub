import { render, screen } from '@testing-library/angular';
import { TestBed } from '@angular/core/testing';
import { BookDetailComponent } from './book-detail';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';

describe('BookDetailComponent', () => {
  it('should render book detail container when valid id provided', async () => {
    const { fixture } = await render(BookDetailComponent, {
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => 'b1'
              }
            }
          }
        }
      ]
    });

    const httpMock = TestBed.inject(HttpTestingController);
    const req1 = httpMock.expectOne('/api/v1/books/b1');
    req1.flush({
      id: 'b1',
      isbn: '12345',
      title: 'Ficciones',
      author: 'Jorge Luis Borges',
      description: 'Cuentos clásicos',
      pageCount: 200,
      publishedYear: 1944
    });

    fixture.detectChanges();

    const req2 = httpMock.expectOne('/api/v1/books/b1/reviews');
    req2.flush([]);

    fixture.detectChanges();

    expect(screen.getByText('Ficciones')).toBeTruthy();
    expect(screen.getByText('por Jorge Luis Borges')).toBeTruthy();
  });
});
