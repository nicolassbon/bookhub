import { render, screen } from '@testing-library/angular';
import { CatalogSearchComponent } from './catalog-search.component';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

describe('CatalogSearchComponent', () => {
  it('should render search input field', async () => {
    await render(CatalogSearchComponent, {
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    expect(screen.getByPlaceholderText(/buscar por título/i)).toBeTruthy();
  });
});
