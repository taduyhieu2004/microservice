import { useMutation } from '@tanstack/react-query';
import { ArrowLeft, Mail, SquareKanban } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { authApi } from '@/features/auth/auth-api';
import { extractErrorMessage } from '@/lib/api';

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [resetToken, setResetToken] = useState<string | null>(null);

  const forgotMutation = useMutation({
    mutationFn: authApi.forgotPassword,
    onSuccess: (data) => setResetToken(data?.reset_token ?? null),
  });

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    forgotMutation.mutate({ email: email.trim() });
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-6">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-sm border border-gray-200 p-8">
        <Link to="/login" className="inline-flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700">
          <ArrowLeft className="w-4 h-4" /> Quay lại đăng nhập
        </Link>

        <div className="flex items-center gap-2 mt-4 mb-6">
          <div className="w-9 h-9 bg-primary-600 rounded-lg flex items-center justify-center">
            <SquareKanban className="w-5 h-5 text-white" />
          </div>
          <span className="font-bold text-xl text-gray-900">TaskFlow</span>
        </div>

        <h2 className="text-2xl font-bold text-gray-900">Quên mật khẩu</h2>
        <p className="text-gray-600 mt-1 text-sm">
          Nhập email tài khoản, hệ thống sẽ tạo token đặt lại mật khẩu (dev: token trả về trực tiếp).
        </p>

        {forgotMutation.isSuccess ? (
          <div className="mt-6 space-y-3">
            <div className="text-sm text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-lg p-3">
              Yêu cầu đã được tạo. Kiểm tra email (môi trường dev: token bên dưới).
            </div>
            {resetToken && (
              <div className="text-xs bg-gray-50 border border-gray-200 rounded-lg p-3 break-all font-mono">
                {resetToken}
              </div>
            )}
          </div>
        ) : (
          <form className="mt-6 space-y-4" onSubmit={onSubmit}>
            <div className="space-y-1.5">
              <Label htmlFor="email">Email</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-3 w-5 h-5 text-gray-400" />
                <Input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="pl-10"
                  required
                />
              </div>
            </div>

            {forgotMutation.isError && (
              <div className="text-sm text-rose-600 bg-rose-50 border border-rose-200 rounded-lg px-3 py-2">
                {extractErrorMessage(forgotMutation.error)}
              </div>
            )}

            <Button type="submit" size="lg" className="w-full" disabled={forgotMutation.isPending}>
              {forgotMutation.isPending ? 'Đang gửi…' : 'Gửi yêu cầu'}
            </Button>
          </form>
        )}
      </div>
    </div>
  );
}
