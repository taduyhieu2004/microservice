import { api } from '@/lib/api';
import type { ApiResponse, PageResponse } from '@/types/api';
import type { ActivityLog } from '@/types/collab';

export const activitiesApi = {
  byTask: (taskId: number) =>
    api
      .get<ApiResponse<PageResponse<ActivityLog> | ActivityLog[]>>(`/activities/tasks/${taskId}`, {
        params: { page: 0, size: 50 },
      })
      .then((res) => {
        const d = res.data.data;
        return Array.isArray(d) ? d : d.content;
      }),

  byProject: (projectId: number) =>
    api
      .get<ApiResponse<PageResponse<ActivityLog> | ActivityLog[]>>(`/activities/projects/${projectId}`, {
        params: { page: 0, size: 50 },
      })
      .then((res) => {
        const d = res.data.data;
        return Array.isArray(d) ? d : d.content;
      }),
};
