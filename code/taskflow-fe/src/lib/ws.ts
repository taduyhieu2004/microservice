import { Client, type IFrame, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const wsUrl = import.meta.env.VITE_WS_URL ?? 'http://localhost:8085/ws/notifications';

interface ConnectOptions {
  token: string;
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (frame: IFrame) => void;
}

export class StompClient {
  private client: Client | null = null;
  private subs: Map<string, () => void> = new Map();

  connect({ token, onConnect, onDisconnect, onError }: ConnectOptions) {
    this.disconnect();
    const url = `${wsUrl}?token=${encodeURIComponent(token)}`;
    this.client = new Client({
      webSocketFactory: () => new SockJS(url) as unknown as WebSocket,
      reconnectDelay: 5_000,
      heartbeatIncoming: 10_000,
      heartbeatOutgoing: 10_000,
      onConnect: () => onConnect?.(),
      onDisconnect: () => onDisconnect?.(),
      onStompError: (frame) => onError?.(frame),
    });
    this.client.activate();
  }

  subscribe<T>(destination: string, handler: (payload: T) => void): () => void {
    if (!this.client) throw new Error('STOMP not connected');
    const sub = this.client.subscribe(destination, (msg: IMessage) => {
      try {
        handler(JSON.parse(msg.body) as T);
      } catch {
        handler(msg.body as unknown as T);
      }
    });
    const unsub = () => sub.unsubscribe();
    this.subs.set(destination, unsub);
    return unsub;
  }

  unsubscribe(destination: string) {
    this.subs.get(destination)?.();
    this.subs.delete(destination);
  }

  disconnect() {
    this.subs.forEach((u) => u());
    this.subs.clear();
    this.client?.deactivate();
    this.client = null;
  }

  get connected() {
    return this.client?.connected ?? false;
  }
}

export const stompClient = new StompClient();
