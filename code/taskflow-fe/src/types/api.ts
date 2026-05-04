export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  total_elements: number;
  total_pages: number;
  last: boolean;
}

export interface ApiErrorPayload {
  code: string;
  message: string;
  errors?: Record<string, string> | null;
  trace_id?: string | null;
}
