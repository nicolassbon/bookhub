import { normalizeHttpError } from './error-normalizer';
import { HttpErrorResponse } from '@angular/common/http';

describe('normalizeHttpError', () => {
  it('should normalize standard backend API error response', () => {
    const httpError = new HttpErrorResponse({
      status: 400,
      error: {
        timestamp: '2026-07-23T20:00:00Z',
        status: 400,
        error: 'Bad Request',
        code: 'VALIDATION_ERROR',
        message: 'displayName must not be blank',
        path: '/api/v1/auth/register'
      }
    });

    const normalized = normalizeHttpError(httpError);
    expect(normalized.status).toBe(400);
    expect(normalized.code).toBe('VALIDATION_ERROR');
    expect(normalized.message).toBe('displayName must not be blank');
  });

  it('should handle network/offline errors gracefully', () => {
    const httpError = new HttpErrorResponse({
      status: 0,
      error: new ProgressEvent('error')
    });

    const normalized = normalizeHttpError(httpError);
    expect(normalized.status).toBe(0);
    expect(normalized.code).toBe('NETWORK_ERROR');
    expect(normalized.message).toContain('servidor');
  });
});
