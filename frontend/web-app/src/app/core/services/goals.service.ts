import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { YearlyGoalResponse, UpdateYearlyGoalRequest } from '../api/contracts/goals';

@Injectable({
  providedIn: 'root'
})
export class GoalsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/goals';

  getYearlyGoal(): Observable<YearlyGoalResponse> {
    return this.http.get<YearlyGoalResponse>(`${this.baseUrl}/yearly`);
  }

  setYearlyGoal(targetBooks: number): Observable<YearlyGoalResponse> {
    const body: UpdateYearlyGoalRequest = { targetBooks };
    return this.http.put<YearlyGoalResponse>(`${this.baseUrl}/yearly`, body);
  }
}
