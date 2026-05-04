import { api, unwrap } from '@/lib/api';
import type { ApiResponse, PageResponse } from '@/types/api';
import type { Comment } from '@/types/collab';

export const commentsApi = {
  list: (taskId: number) =>
    api
      .get<ApiResponse<PageResponse<Comment> | Comment[]>>(`/tasks/${taskId}/comments`, {
        params: { page: 0, size: 100 },
      })
      .then((res) => {
        const d = res.data.data;
        return Array.isArray(d) ? d : d.content;
      }),

  create: (taskId: number, content: string, parentId?: number) =>
    api
      .post<ApiResponse<Comment>>(`/tasks/${taskId}/comments`, { content, parent_id: parentId })
      .then(unwrap),

  update: (id: number, content: string) =>
    api.put<ApiResponse<Comment>>(`/comments/${id}`, { content }).then(unwrap),

  remove: (id: number) =>
    api.delete<ApiResponse<void>>(`/comments/${id}`).then(unwrap),
};
