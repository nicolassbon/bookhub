import { HttpErrorResponse } from '@angular/common/http';
import { ApiErrorResponse, NormalizedApiError } from './contracts/error';

export function normalizeHttpError(error: unknown): NormalizedApiError {
  if (error instanceof NormalizedApiError) {
    return error;
  }

  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return new NormalizedApiError(
        0,
        'NETWORK_ERROR',
        'No se pudo conectar con el servidor. Verifica tu conexión a internet.',
        error.url || '',
        new Date().toISOString()
      );
    }

    if (error.error && typeof error.error === 'object') {
      const apiErr = error.error as ApiErrorResponse;
      return NormalizedApiError.fromApiError(error.status, apiErr);
    }

    return new NormalizedApiError(
      error.status,
      `HTTP_${error.status}`,
      error.message || 'Error en la petición HTTP.',
      error.url || '',
      new Date().toISOString()
    );
  }

  return new NormalizedApiError(
    500,
    'UNKNOWN_ERROR',
    error instanceof Error ? error.message : 'Error desconocido.',
    '',
    new Date().toISOString()
  );
}
