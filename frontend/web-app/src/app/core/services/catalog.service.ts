import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookResponse, BookSearchResponse } from '../api/contracts/catalog';

@Injectable({
  providedIn: 'root'
})
export class CatalogService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/books';

  searchBooks(query = '', page = 0, size = 12): Observable<BookSearchResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (query.trim()) {
      params = params.set('q', query.trim());
    }

    return this.http.get<BookSearchResponse>(this.baseUrl, { params });
  }

  getBookById(id: string): Observable<BookResponse> {
    return this.http.get<BookResponse>(`${this.baseUrl}/${id}`);
  }
}
