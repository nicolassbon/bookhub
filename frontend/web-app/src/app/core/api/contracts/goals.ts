export interface YearlyGoalResponse {
  id: string;
  userId: string;
  year: number;
  targetBooks: number;
  booksReadCount: number;
  percentageAchieved: number;
  isAchieved: boolean;
}

export interface UpdateYearlyGoalRequest {
  targetBooks: number;
}
