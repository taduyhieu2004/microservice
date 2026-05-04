import { api, unwrap } from '@/lib/api';
import type { ApiResponse, PageResponse } from '@/types/api';
import type { Attachment } from '@/types/collab';

export const attachmentsApi = {
  list: (taskId: number) =>
    api
      .get<ApiResponse<PageResponse<Attachment> | Attachment[]>>(`/tasks/${taskId}/attachments`, {
        params: { page: 0, size: 100 },
      })
      .then((res) => {
        const d = res.data.data;
        return Array.isArray(d) ? d : d.content;
      }),

  upload: (taskId: number, file: File) => {
    const fd = new FormData();
    fd.append('file', file);
    return api
      .post<ApiResponse<Attachment>>(`/tasks/${taskId}/attachments`, fd, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then(unwrap);
  },

  remove: (id: number) =>
    api.delete<ApiResponse<void>>(`/attachments/${id}`).then(unwrap),

  downloadUrl: (id: number) => {
    const base = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1';
    return `${base}/attachments/${id}/download`;
  },
};
