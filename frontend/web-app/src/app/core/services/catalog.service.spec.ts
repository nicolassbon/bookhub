import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { CatalogService } from './catalog.service';

describe('CatalogService', () => {
  let service: CatalogService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CatalogService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(CatalogService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should query books with pagination', () => {
    service.searchBooks('Dune', 0, 10).subscribe(res => {
      expect(res.content.length).toBe(1);
      expect(res.content[0].title).toBe('Dune');
    });

    const req = httpMock.expectOne(request => request.url === '/api/v1/books' && request.params.get('q') === 'Dune');
    expect(req.request.method).toBe('GET');

    req.flush({
      content: [{ id: 'b1', isbn: '123', title: 'Dune', author: 'Frank Herbert', description: '', pageCount: 412 }],
      pageNumber: 0,
      pageSize: 10,
      totalElements: 1,
      totalPages: 1
    });
  });

  it('should fetch book detail by id', () => {
    service.getBookById('b1').subscribe(book => {
      expect(book.title).toBe('Dune');
    });

    const req = httpMock.expectOne('/api/v1/books/b1');
    expect(req.request.method).toBe('GET');

    req.flush({ id: 'b1', isbn: '123', title: 'Dune', author: 'Frank Herbert', description: '', pageCount: 412 });
  });
});
