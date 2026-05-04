import { LogOut } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useAuthStore } from '@/stores/auth-store';

export function DashboardPage() {
  const { user, logout } = useAuthStore();
  return (
    <div className="min-h-screen flex items-center justify-center p-6">
      <div className="max-w-lg text-center">
        <div className="text-5xl mb-4">🎉</div>
        <h1 className="text-2xl font-bold text-gray-900">Đăng nhập thành công</h1>
        <p className="text-gray-600 mt-2">
          Xin chào <b>{user?.username}</b>. Phase 1 đã hoàn tất — auth flow hoạt động.
        </p>
        <p className="text-sm text-gray-500 mt-4">
          Layout đầy đủ + danh sách project sẽ có ở Phase 3.
        </p>
        <Button variant="secondary" className="mt-6" onClick={logout}>
          <LogOut className="w-4 h-4" /> Đăng xuất
        </Button>
      </div>
    </div>
  );
}
