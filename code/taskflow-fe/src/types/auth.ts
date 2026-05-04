export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  full_name?: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  new_password: string;
}

export interface RefreshRequest {
  refresh_token: string;
}

export interface LoginResponse {
  id: number;
  username: string;
  access_token: string;
  refresh_token: string;
  token_expired_seconds: number;
  refresh_expired_seconds: number;
  token_type: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
  full_name?: string | null;
  avatar_url?: string | null;
  bio?: string | null;
  dob?: string | null;
  status?: string;
  last_login_at?: number | null;
  created_at?: number | null;
}
