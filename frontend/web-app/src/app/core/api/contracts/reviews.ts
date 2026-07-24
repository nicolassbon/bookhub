export interface ReviewResponse {
  id: string;
  userId: string;
  userDisplayName?: string;
  bookId: string;
  rating: number;
  content: string;
  status: 'VISIBLE' | 'HIDDEN';
  createdAt: string;
  updatedAt?: string;
}

export interface CreateReviewRequest {
  bookId: string;
  rating: number;
  content: string;
}

export interface UpdateReviewRequest {
  rating: number;
  content: string;
}
