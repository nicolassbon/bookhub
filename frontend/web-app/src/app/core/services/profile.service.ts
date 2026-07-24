import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserResponse } from '../api/contracts/auth';
import { UpdateProfileRequest } from '../api/contracts/profile';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private readonly http = inject(HttpClient);

  getProfile(): Observable<UserResponse> {
    return this.http.get<UserResponse>('/api/v1/users/me');
  }

  updateProfile(displayName: string): Observable<UserResponse> {
    const body: UpdateProfileRequest = { displayName };
    return this.http.patch<UserResponse>('/api/v1/users/me', body);
  }
}
