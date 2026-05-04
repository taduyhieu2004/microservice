import { useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { stompClient } from '@/lib/ws';
import { useAuthStore } from '@/stores/auth-store';
import type { Notification } from '@/types/notification';

export function useNotificationsStream() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const queryClient = useQueryClient();

  useEffect(() => {
    if (!accessToken) {
      stompClient.disconnect();
      return;
    }

    let unsub: (() => void) | undefined;

    stompClient.connect({
      token: accessToken,
      onConnect: () => {
        unsub = stompClient.subscribe<Notification>('/user/queue/notifications', (notif) => {
          queryClient.setQueryData<{ count: number } | undefined>(
            ['notifications', 'unread'],
            (old) => ({ count: (old?.count ?? 0) + 1 }),
          );
          queryClient.invalidateQueries({ queryKey: ['notifications', 'list'] });

          if (typeof window !== 'undefined' && 'Notification' in window && window.Notification.permission === 'granted') {
            new window.Notification(notif.title, { body: notif.body ?? undefined });
          }
        });
      },
    });

    return () => {
      unsub?.();
      stompClient.disconnect();
    };
  }, [accessToken, queryClient]);
}
