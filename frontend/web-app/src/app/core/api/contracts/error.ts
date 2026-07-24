export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  code: string;
  message: string;
  path: string;
}

export class NormalizedApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    message: string,
    public readonly path: string,
    public readonly timestamp: string
  ) {
    super(message);
    this.name = 'NormalizedApiError';
  }

  static fromApiError(status: number, error: ApiErrorResponse): NormalizedApiError {
    return new NormalizedApiError(
      status,
      error.code || 'UNKNOWN_ERROR',
      error.message || 'Ocurrió un error inesperado.',
      error.path || '',
      error.timestamp || new Date().toISOString()
    );
  }
}
