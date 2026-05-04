import { Bell, Moon, Search, SquareKanban } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Avatar } from '@/components/ui/avatar';
import { useAuthStore } from '@/stores/auth-store';

export function Topbar() {
  const user = useAuthStore((s) => s.user);
  return (
    <header className="bg-white border-b border-gray-200 fixed top-0 left-0 right-0 z-30 h-14">
      <div className="h-full px-4 flex items-center justify-between gap-4">
        <Link to="/dashboard" className="flex items-center gap-2">
          <div className="w-8 h-8 bg-primary-600 rounded-md flex items-center justify-center">
            <SquareKanban className="w-4 h-4 text-white" />
          </div>
          <span className="font-bold text-gray-900">TaskFlow</span>
        </Link>

        <div className="flex-1 max-w-md mx-auto">
          <div className="relative">
            <Search className="absolute left-3 top-2.5 w-4 h-4 text-gray-400" />
            <input
              placeholder="Tìm project, task, member..."
              className="w-full pl-9 pr-4 py-2 bg-gray-100 border-0 rounded-lg text-sm focus:ring-2 focus:ring-primary-500 focus:bg-white outline-none"
            />
          </div>
        </div>

        <div className="flex items-center gap-1">
          <button className="p-2 text-gray-600 hover:bg-gray-100 rounded-lg" title="Dark mode">
            <Moon className="w-5 h-5" />
          </button>
          <button className="p-2 text-gray-600 hover:bg-gray-100 rounded-lg relative" title="Thông báo">
            <Bell className="w-5 h-5" />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full ring-2 ring-white" />
          </button>
          <button className="flex items-center gap-2 p-1 hover:bg-gray-100 rounded-lg" title={user?.username}>
            <Avatar name={user?.full_name ?? user?.username} seed={user?.id} size="md" />
          </button>
        </div>
      </div>
    </header>
  );
}
