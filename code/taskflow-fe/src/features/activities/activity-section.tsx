import { useQuery } from '@tanstack/react-query';
import { Activity } from 'lucide-react';
import { activitiesApi } from '@/features/activities/activities-api';
import { Avatar } from '@/components/ui/avatar';
import { formatRelativeTime } from '@/lib/utils';

interface Props {
  taskId: number;
}

const ACTION_LABEL: Record<string, string> = {
  TASK_CREATED: 'tạo task',
  TASK_UPDATED: 'cập nhật task',
  TASK_MOVED: 'di chuyển task',
  TASK_DELETED: 'xoá task',
  TASK_ASSIGNED: 'giao task',
  COMMENT_CREATED: 'thêm bình luận',
  COMMENT_UPDATED: 'sửa bình luận',
  COMMENT_DELETED: 'xoá bình luận',
  ATTACHMENT_UPLOADED: 'tải lên file',
  ATTACHMENT_DELETED: 'xoá file',
};

export function ActivitySection({ taskId }: Props) {
  const { data: activities = [], isLoading } = useQuery({
    queryKey: ['activities', 'task', taskId],
    queryFn: () => activitiesApi.byTask(taskId),
  });

  return (
    <div className="p-5 bg-gray-50 min-h-full">
      <h3 className="text-sm font-semibold text-gray-900 flex items-center gap-2 mb-3">
        <Activity className="w-4 h-4" /> Lịch sử hoạt động
      </h3>

      {isLoading ? (
        <p className="text-xs text-gray-400">Đang tải…</p>
      ) : activities.length === 0 ? (
        <p className="text-sm text-gray-400 text-center py-8">Chưa có hoạt động nào.</p>
      ) : (
        <div className="space-y-3">
          {activities.map((a) => (
            <div key={a.id} className="flex gap-3 items-start">
              <Avatar seed={a.actor_id} name={`U${a.actor_id}`} size="sm" />
              <div className="flex-1 min-w-0">
                <p className="text-sm text-gray-700">
                  <span className="font-medium text-gray-900">User #{a.actor_id}</span>{' '}
                  {ACTION_LABEL[a.action] ?? a.action.toLowerCase()}
                </p>
                <p className="text-xs text-gray-400">{formatRelativeTime(a.occurred_at)}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
