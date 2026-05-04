import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { projectsApi } from '@/features/projects/projects-api';
import { extractErrorMessage } from '@/lib/api';

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function CreateProjectDialog({ open, onOpenChange }: Props) {
  const queryClient = useQueryClient();
  const [name, setName] = useState('');
  const [key, setKey] = useState('');
  const [description, setDescription] = useState('');

  const mutation = useMutation({
    mutationFn: projectsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      reset();
      onOpenChange(false);
    },
  });

  function reset() {
    setName('');
    setKey('');
    setDescription('');
    mutation.reset();
  }

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    mutation.mutate({
      name: name.trim(),
      key: key.trim().toUpperCase(),
      description: description.trim() || undefined,
    });
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(o) => {
        if (!o) reset();
        onOpenChange(o);
      }}
    >
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Tạo project mới</DialogTitle>
          <DialogDescription>Nhập thông tin project — bạn sẽ tự động trở thành Owner.</DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="name">Tên project</Label>
            <Input
              id="name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              maxLength={255}
              placeholder="Ví dụ: TaskFlow Mobile"
              autoFocus
            />
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="key">Key (2–10 ký tự, chữ in hoa)</Label>
            <Input
              id="key"
              value={key}
              onChange={(e) => setKey(e.target.value.toUpperCase())}
              required
              maxLength={10}
              pattern="^[A-Z][A-Z0-9]{1,9}$"
              placeholder="TFM"
              className="uppercase"
            />
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="description">Mô tả (tuỳ chọn)</Label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              maxLength={2000}
              rows={3}
              className="w-full px-3 py-2.5 border border-gray-300 rounded-lg text-sm placeholder:text-gray-400 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none resize-none"
              placeholder="Project làm gì?"
            />
          </div>

          {mutation.isError && (
            <div className="text-sm text-rose-600 bg-rose-50 border border-rose-200 rounded-lg px-3 py-2">
              {extractErrorMessage(mutation.error)}
            </div>
          )}

          <DialogFooter>
            <DialogClose asChild>
              <Button type="button" variant="secondary">
                Huỷ
              </Button>
            </DialogClose>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? 'Đang tạo…' : 'Tạo project'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
