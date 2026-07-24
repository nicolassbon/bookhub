import { render, screen } from '@testing-library/angular';
import { TestBed } from '@angular/core/testing';
import { BookDetailComponent } from './book-detail.component';
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
            snapshot: { paramMap: { get: () => 'b1' } }
          }
        }
      ]
    });

    const httpMock = TestBed.inject(HttpTestingController);
    const req = httpMock.expectOne('/api/v1/books/b1');
    req.flush({ id: 'b1', isbn: '123', title: 'Fahrenheit 451', author: 'Ray Bradbury', description: '', pageCount: 200 });

    fixture.detectChanges();

    expect(screen.getByText('Fahrenheit 451')).toBeTruthy();
  });
});
