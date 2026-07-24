export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: UserResponse;
}

export interface RegisterRequest {
  email: string;
  password: string;
  displayName: string;
}

export interface UserResponse {
  id: string;
  email: string;
  displayName: string;
  role: 'ROLE_USER' | 'ROLE_ADMIN';
  createdAt: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}
