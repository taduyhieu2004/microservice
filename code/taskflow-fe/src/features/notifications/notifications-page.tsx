import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Bell, Check, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { NotificationIcon } from '@/features/notifications/notification-icon';
import { notificationsApi } from '@/features/notifications/notifications-api';
import { cn, formatRelativeTime } from '@/lib/utils';

export function NotificationsPage() {
  const queryClient = useQueryClient();
  const [unreadOnly, setUnreadOnly] = useState(false);
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ['notifications', 'list', unreadOnly, page],
    queryFn: () => notificationsApi.list(unreadOnly, page, 20),
  });

  const markAll = useMutation({
    mutationFn: () => notificationsApi.markAllRead(),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] }),
  });
  const markRead = useMutation({
    mutationFn: (id: number) => notificationsApi.markRead(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] }),
  });
  const remove = useMutation({
    mutationFn: (id: number) => notificationsApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] }),
  });

  const items = data?.content ?? [];
  const totalPages = data?.total_pages ?? 0;

  return (
    <div className="p-8 max-w-4xl mx-auto">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <Bell className="w-6 h-6" /> Thông báo
        </h1>
        <button
          onClick={() => markAll.mutate()}
          disabled={markAll.isPending}
          className="px-3 py-2 text-sm border border-gray-300 rounded-lg bg-white hover:bg-gray-50 flex items-center gap-2 text-gray-700"
        >
          <Check className="w-4 h-4" /> Đánh dấu tất cả đã đọc
        </button>
      </div>

      <div className="mt-6 inline-flex border border-gray-200 rounded-lg p-1 bg-white">
        <button
          onClick={() => { setUnreadOnly(false); setPage(0); }}
          className={cn(
            'px-3 py-1.5 text-sm rounded-md transition',
            !unreadOnly ? 'bg-primary-50 text-primary-700 font-medium' : 'text-gray-600 hover:bg-gray-50',
          )}
        >
          Tất cả
        </button>
        <button
          onClick={() => { setUnreadOnly(true); setPage(0); }}
          className={cn(
            'px-3 py-1.5 text-sm rounded-md transition',
            unreadOnly ? 'bg-primary-50 text-primary-700 font-medium' : 'text-gray-600 hover:bg-gray-50',
          )}
        >
          Chưa đọc
        </button>
      </div>

      <div className="mt-6 bg-white rounded-xl border border-gray-200 divide-y divide-gray-100">
        {isLoading ? (
          <div className="p-8 text-center text-sm text-gray-400">Đang tải…</div>
        ) : items.length === 0 ? (
          <div className="p-12 text-center">
            <Bell className="w-10 h-10 text-gray-300 mx-auto" />
            <p className="text-sm text-gray-500 mt-2">Không có thông báo nào.</p>
          </div>
        ) : (
          items.map((n) => {
            const unread = n.read_at == null;
            const Inner = (
              <div className="p-4 flex gap-3 items-start group hover:bg-gray-50 transition">
                <NotificationIcon type={n.type} />
                <div className="flex-1 min-w-0">
                  <p className="text-sm text-gray-900">{n.title}</p>
                  {n.body && <p className="text-sm text-gray-600 mt-0.5">{n.body}</p>}
                  <p className="text-xs text-gray-400 mt-1">{formatRelativeTime(n.created_at)}</p>
                </div>
                {unread && <span className="w-2 h-2 bg-primary-500 rounded-full mt-2 flex-shrink-0" />}
                <div className="opacity-0 group-hover:opacity-100 flex gap-1 transition">
                  {unread && (
                    <button
                      onClick={(e) => {
                        e.preventDefault();
                        markRead.mutate(n.id);
                      }}
                      className="p-1.5 text-gray-400 hover:text-primary-600 hover:bg-primary-50 rounded"
                      title="Đánh dấu đã đọc"
                    >
                      <Check className="w-4 h-4" />
                    </button>
                  )}
                  <button
                    onClick={(e) => {
                      e.preventDefault();
                      remove.mutate(n.id);
                    }}
                    className="p-1.5 text-gray-400 hover:text-rose-600 hover:bg-rose-50 rounded"
                    title="Xoá"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            );
            return n.link ? (
              <Link key={n.id} to={n.link} className={cn('block', unread && 'bg-primary-50/30')}>
                {Inner}
              </Link>
            ) : (
              <div key={n.id} className={cn(unread && 'bg-primary-50/30')}>
                {Inner}
              </div>
            );
          })
        )}
      </div>

      {totalPages > 1 && (
        <div className="mt-4 flex justify-center gap-2">
          <button
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
            className="px-3 py-1.5 text-sm border border-gray-300 rounded-lg bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Trước
          </button>
          <span className="px-3 py-1.5 text-sm text-gray-600">{page + 1} / {totalPages}</span>
          <button
            disabled={page + 1 >= totalPages}
            onClick={() => setPage((p) => p + 1)}
            className="px-3 py-1.5 text-sm border border-gray-300 rounded-lg bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Sau
          </button>
        </div>
      )}
    </div>
  );
}
