import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Bell } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { NotificationIcon } from '@/features/notifications/notification-icon';
import { notificationsApi } from '@/features/notifications/notifications-api';
import { cn, formatRelativeTime } from '@/lib/utils';
import type { Notification } from '@/types/notification';

export function NotificationsDropdown() {
  const queryClient = useQueryClient();

  const { data: unread } = useQuery({
    queryKey: ['notifications', 'unread'],
    queryFn: notificationsApi.unreadCount,
    refetchInterval: 60_000,
  });

  const { data } = useQuery({
    queryKey: ['notifications', 'list', 'recent'],
    queryFn: () => notificationsApi.list(false, 0, 5),
    refetchInterval: 60_000,
  });

  const markAllMutation = useMutation({
    mutationFn: () => notificationsApi.markAllRead(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });

  const markReadMutation = useMutation({
    mutationFn: (id: number) => notificationsApi.markRead(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] }),
  });

  const items = data?.content ?? [];
  const unreadCount = unread?.count ?? 0;

  return (
    <Popover>
      <PopoverTrigger asChild>
        <button className="p-2 text-gray-600 hover:bg-gray-100 rounded-lg relative" title="Thông báo">
          <Bell className="w-5 h-5" />
          {unreadCount > 0 && (
            <span className="absolute top-1 right-1 min-w-4 h-4 px-1 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center ring-2 ring-white">
              {unreadCount > 99 ? '99+' : unreadCount}
            </span>
          )}
        </button>
      </PopoverTrigger>
      <PopoverContent className="w-96 p-0">
        <div className="p-4 border-b border-gray-100 flex items-center justify-between">
          <h3 className="font-semibold text-gray-900">Thông báo</h3>
          {unreadCount > 0 && (
            <button
              onClick={() => markAllMutation.mutate()}
              disabled={markAllMutation.isPending}
              className="text-xs text-primary-600 hover:text-primary-700 font-medium"
            >
              Đánh dấu tất cả đã đọc
            </button>
          )}
        </div>
        <div className="max-h-96 overflow-y-auto scrollbar-thin">
          {items.length === 0 ? (
            <div className="p-8 text-center text-sm text-gray-400">Không có thông báo nào.</div>
          ) : (
            items.map((n) => (
              <Item key={n.id} notif={n} onMarkRead={() => markReadMutation.mutate(n.id)} />
            ))
          )}
        </div>
        <div className="p-3 border-t border-gray-100 text-center">
          <Link to="/notifications" className="text-sm text-primary-600 hover:text-primary-700 font-medium">
            Xem tất cả
          </Link>
        </div>
      </PopoverContent>
    </Popover>
  );
}

function Item({ notif, onMarkRead }: { notif: Notification; onMarkRead: () => void }) {
  const unread = notif.read_at == null;
  const content = (
    <div className="flex gap-3">
      <NotificationIcon type={notif.type} />
      <div className="flex-1 min-w-0">
        <p className="text-sm text-gray-900 line-clamp-1">{notif.title}</p>
        {notif.body && <p className="text-xs text-gray-500 mt-0.5 line-clamp-2">{notif.body}</p>}
        <p className="text-xs text-gray-400 mt-1">{formatRelativeTime(notif.created_at)}</p>
      </div>
      {unread && <span className="w-2 h-2 bg-primary-500 rounded-full mt-2 flex-shrink-0" />}
    </div>
  );
  const baseClass = cn(
    'block p-4 hover:bg-gray-50 border-b border-gray-100 transition',
    unread && 'bg-primary-50/30',
  );
  if (notif.link) {
    return (
      <Link to={notif.link} className={baseClass} onClick={() => unread && onMarkRead()}>
        {content}
      </Link>
    );
  }
  return (
    <button onClick={() => unread && onMarkRead()} className={cn(baseClass, 'w-full text-left')}>
      {content}
    </button>
  );
}
