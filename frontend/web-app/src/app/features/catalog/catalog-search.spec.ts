import { render, screen } from '@testing-library/angular';
import { TestBed } from '@angular/core/testing';
import { CatalogSearchComponent } from './catalog-search';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

describe('CatalogSearchComponent', () => {
  it('should render catalog header and search input', async () => {
    const { fixture } = await render(CatalogSearchComponent, {
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    const httpMock = TestBed.inject(HttpTestingController);
    const req = httpMock.expectOne(r => r.url === '/api/v1/books');
    req.flush({
      content: [],
      pageNumber: 0,
      pageSize: 12,
      totalElements: 0,
      totalPages: 0
    });

    fixture.detectChanges();

    expect(screen.getByText('Explorá el Catálogo de Libros')).toBeTruthy();
    expect(screen.getByRole('searchbox', { name: /buscar libros/i })).toBeTruthy();
  });
});
