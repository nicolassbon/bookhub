import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { LibraryService } from './library.service';

describe('LibraryService', () => {
  let service: LibraryService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        LibraryService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(LibraryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch user library books', () => {
    service.getUserBooks('READING').subscribe(books => {
      expect(books.length).toBe(1);
    });

    const req = httpMock.expectOne('/api/v1/library/me/books?state=READING');
    expect(req.request.method).toBe('GET');

    req.flush([
      {
        id: 'ub1',
        userId: 'u1',
        bookId: 'b1',
        book: { id: 'b1', isbn: '123', title: 'Dune', author: 'Herbert', description: '', pageCount: 400 },
        state: 'READING',
        currentPage: 150,
        totalPages: 400,
        percentageCompleted: 37.5
      }
    ]);
  });

  it('should add book to library', () => {
    service.addBookToLibrary('b1', 'READING').subscribe(res => {
      expect(res.id).toBe('ub1');
    });

    const req = httpMock.expectOne('/api/v1/library/books');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ bookId: 'b1', state: 'READING' });

    req.flush({
      id: 'ub1',
      userId: 'u1',
      bookId: 'b1',
      book: { id: 'b1', isbn: '123', title: 'Dune', author: 'Herbert', description: '', pageCount: 400 },
      state: 'READING',
      currentPage: 0,
      totalPages: 400,
      percentageCompleted: 0
    });
  });

  it('should update reading progress', () => {
    service.updateReadingProgress('ub1', 200).subscribe(res => {
      expect(res.currentPage).toBe(200);
    });

    const req = httpMock.expectOne('/api/v1/library/books/ub1/progress');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ currentPage: 200 });

    req.flush({
      id: 'ub1',
      userId: 'u1',
      bookId: 'b1',
      book: { id: 'b1', isbn: '123', title: 'Dune', author: 'Herbert', description: '', pageCount: 400 },
      state: 'READING',
      currentPage: 200,
      totalPages: 400,
      percentageCompleted: 50.0
    });
  });
});
