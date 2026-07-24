import { render, screen } from '@testing-library/angular';
import { TestBed } from '@angular/core/testing';
import { HomeComponent } from './home';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

describe('HomeComponent', () => {
  it('should render home hero section and featured books', async () => {
    const { fixture } = await render(HomeComponent, {
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    const httpMock = TestBed.inject(HttpTestingController);
    const req = httpMock.expectOne(r => r.url === '/api/v1/books');
    req.flush({
      content: [{ id: 'b1', isbn: '123', title: 'Cien años de soledad', author: 'Gabriel García Márquez', description: '', pageCount: 400 }],
      pageNumber: 0,
      pageSize: 4,
      totalElements: 1,
      totalPages: 1
    });

    fixture.detectChanges();

    expect(screen.getByText(/Bienvenido a BookHub/i)).toBeTruthy();
    expect(screen.getByText('Cien años de soledad')).toBeTruthy();
  });
});
