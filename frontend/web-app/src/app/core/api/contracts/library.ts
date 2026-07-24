import { BookResponse } from './catalog';

export type ReadingState = 'WANT_TO_READ' | 'READING' | 'READ';

export interface UserBookResponse {
  id: string;
  userId: string;
  bookId: string;
  book: BookResponse;
  state: ReadingState;
  currentPage: number;
  totalPages: number;
  percentageCompleted: number;
  startedAt?: string;
  finishedAt?: string;
}

export interface AddBookToLibraryRequest {
  bookId: string;
  state: ReadingState;
}

export interface UpdateProgressRequest {
  currentPage: number;
}
