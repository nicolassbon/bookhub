import { render, screen } from '@testing-library/angular';
import { TestBed } from '@angular/core/testing';
import { LibraryListComponent } from './library-list';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

describe('LibraryListComponent', () => {
  it('should render filter tabs and user books', async () => {
    const { fixture } = await render(LibraryListComponent, {
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    const httpMock = TestBed.inject(HttpTestingController);
    const req = httpMock.expectOne('/api/v1/library/me/books');
    req.flush([
      {
        id: 'ub1',
        userId: 'u1',
        bookId: 'b1',
        book: { id: 'b1', isbn: '123', title: 'Rayuela', author: 'Julio Cortázar', description: '', pageCount: 600 },
        state: 'READING',
        currentPage: 150,
        totalPages: 600,
        percentageCompleted: 25.0
      }
    ]);

    fixture.detectChanges();

    expect(screen.getByRole('tab', { name: 'Todos' })).toBeTruthy();
    expect(screen.getByText('Rayuela')).toBeTruthy();
  });
});
