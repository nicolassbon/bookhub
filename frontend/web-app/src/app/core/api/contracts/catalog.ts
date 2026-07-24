export interface BookResponse {
  id: string;
  isbn: string;
  title: string;
  author: string;
  description: string;
  pageCount: number;
  coverUrl?: string;
  publishedYear?: number;
}

export interface BookSearchResponse {
  content: BookResponse[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}
