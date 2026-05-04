export interface Notification {
  id: number;
  user_id: number;
  type: string;
  title: string;
  body?: string | null;
  link?: string | null;
  read_at?: number | null;
  metadata?: Record<string, unknown> | null;
  created_at: number;
}

export interface NotificationPreference {
  user_id: number;
  in_app_enabled: boolean;
  email_enabled: boolean;
  per_type_settings?: Record<string, boolean>;
}

export interface UpdatePreferenceRequest {
  in_app_enabled?: boolean;
  email_enabled?: boolean;
  per_type_settings?: Record<string, boolean>;
}
