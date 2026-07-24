import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserBookResponse, AddBookToLibraryRequest, UpdateProgressRequest, ReadingState } from '../api/contracts/library';

@Injectable({
  providedIn: 'root'
})
export class LibraryService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/library';

  getUserBooks(state?: ReadingState): Observable<UserBookResponse[]> {
    let params = new HttpParams();
    if (state) {
      params = params.set('state', state);
    }
    return this.http.get<UserBookResponse[]>(`${this.baseUrl}/me/books`, { params });
  }

  addBookToLibrary(bookId: string, state: ReadingState): Observable<UserBookResponse> {
    const body: AddBookToLibraryRequest = { bookId, state };
    return this.http.post<UserBookResponse>(`${this.baseUrl}/books`, body);
  }

  updateReadingProgress(entryId: string, currentPage: number): Observable<UserBookResponse> {
    const body: UpdateProgressRequest = { currentPage };
    return this.http.patch<UserBookResponse>(`${this.baseUrl}/books/${entryId}/progress`, body);
  }

  removeBookFromLibrary(entryId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/books/${entryId}`);
  }
}
