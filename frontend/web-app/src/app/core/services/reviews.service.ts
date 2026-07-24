import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ReviewResponse, CreateReviewRequest, UpdateReviewRequest } from '../api/contracts/reviews';

@Injectable({
  providedIn: 'root'
})
export class ReviewsService {
  private readonly http = inject(HttpClient);

  getReviewsForBook(bookId: string): Observable<ReviewResponse[]> {
    return this.http.get<ReviewResponse[]>(`/api/v1/books/${bookId}/reviews`);
  }

  createReview(bookId: string, rating: number, content: string): Observable<ReviewResponse> {
    const body: CreateReviewRequest = { bookId, rating, content };
    return this.http.post<ReviewResponse>('/api/v1/reviews', body);
  }

  updateReview(reviewId: string, rating: number, content: string): Observable<ReviewResponse> {
    const body: UpdateReviewRequest = { rating, content };
    return this.http.patch<ReviewResponse>(`/api/v1/reviews/${reviewId}`, body);
  }
}
