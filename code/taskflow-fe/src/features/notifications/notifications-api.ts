import { api, unwrap } from '@/lib/api';
import type { ApiResponse, PageResponse } from '@/types/api';
import type { Notification, NotificationPreference, UpdatePreferenceRequest } from '@/types/notification';

export const notificationsApi = {
  list: (unreadOnly = false, page = 0, size = 20) =>
    api
      .get<ApiResponse<PageResponse<Notification>>>('/notifications', {
        params: { unread_only: unreadOnly, page, size },
      })
      .then(unwrap),

  unreadCount: () =>
    api.get<ApiResponse<{ count: number }>>('/notifications/unread-count').then(unwrap),

  markRead: (id: number) =>
    api.patch<ApiResponse<Notification>>(`/notifications/${id}/read`).then(unwrap),

  markAllRead: () =>
    api.patch<ApiResponse<{ updated: number }>>('/notifications/read-all').then(unwrap),

  remove: (id: number) =>
    api.delete<ApiResponse<void>>(`/notifications/${id}`).then(unwrap),

  preference: () =>
    api.get<ApiResponse<NotificationPreference>>('/notifications/preferences').then(unwrap),

  updatePreference: (req: UpdatePreferenceRequest) =>
    api.put<ApiResponse<NotificationPreference>>('/notifications/preferences', req).then(unwrap),
};
