import { render, screen } from '@testing-library/angular';
import { TestBed } from '@angular/core/testing';
import { LibraryListComponent } from './library-list.component';
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
        book: { id: 'b1', isbn: '123', title: 'El Aleph', author: 'Jorge Luis Borges', description: '', pageCount: 180 },
        state: 'READING',
        currentPage: 90,
        totalPages: 180,
        percentageCompleted: 50.0
      }
    ]);

    fixture.detectChanges();

    expect(screen.getByText('El Aleph')).toBeTruthy();
    expect(screen.getByText('50%')).toBeTruthy();
  });
});
