import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { CheckSquare, Plus, X } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import { checklistsApi } from '@/features/checklists/checklists-api';
import { cn } from '@/lib/utils';

interface Props {
  taskId: number;
}

export function ChecklistSection({ taskId }: Props) {
  const queryClient = useQueryClient();
  const [adding, setAdding] = useState(false);
  const [title, setTitle] = useState('');

  const { data: checklists = [] } = useQuery({
    queryKey: ['checklists', taskId],
    queryFn: () => checklistsApi.list(taskId),
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['checklists', taskId] });

  const createMutation = useMutation({
    mutationFn: (t: string) => checklistsApi.create(taskId, t),
    onSuccess: () => {
      invalidate();
      setTitle('');
      setAdding(false);
    },
  });

  function onCreate(e: FormEvent) {
    e.preventDefault();
    if (!title.trim()) return;
    createMutation.mutate(title.trim());
  }

  const totalItems = checklists.reduce((s, c) => s + c.items.length, 0);
  const doneItems = checklists.reduce((s, c) => s + c.items.filter((i) => i.completed).length, 0);
  const pct = totalItems === 0 ? 0 : Math.round((doneItems / totalItems) * 100);

  return (
    <div className="px-5 pb-4 border-t border-gray-100 pt-4">
      <div className="flex items-center justify-between mb-2">
        <h3 className="text-sm font-semibold text-gray-900 flex items-center gap-2">
          <CheckSquare className="w-4 h-4" /> Checklist
          {totalItems > 0 && (
            <span className="text-xs text-gray-500 font-normal">{doneItems}/{totalItems}</span>
          )}
        </h3>
        <button
          onClick={() => setAdding(true)}
          className="text-xs text-primary-600 hover:text-primary-700 font-medium flex items-center gap-1"
        >
          <Plus className="w-3 h-3" /> Thêm
        </button>
      </div>

      {totalItems > 0 && (
        <div className="h-1 bg-gray-100 rounded-full overflow-hidden mb-3">
          <div className="h-full bg-emerald-500 transition-all" style={{ width: `${pct}%` }} />
        </div>
      )}

      {adding && (
        <form onSubmit={onCreate} className="flex gap-2 mb-3">
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            onKeyDown={(e) => e.key === 'Escape' && setAdding(false)}
            placeholder="Tên checklist…"
            autoFocus
            className="flex-1 px-2.5 py-1.5 text-sm border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 outline-none"
          />
          <button type="submit" className="px-2.5 py-1 text-xs bg-primary-600 hover:bg-primary-700 text-white rounded-md">
            Lưu
          </button>
          <button type="button" onClick={() => setAdding(false)} className="text-gray-400 hover:text-gray-700">
            <X className="w-4 h-4" />
          </button>
        </form>
      )}

      <div className="space-y-3">
        {checklists.map((cl) => (
          <ChecklistGroup key={cl.id} checklistId={cl.id} title={cl.title} items={cl.items} taskId={taskId} />
        ))}
        {checklists.length === 0 && !adding && (
          <p className="text-xs text-gray-400">Chưa có checklist nào.</p>
        )}
      </div>
    </div>
  );
}

function ChecklistGroup({
  checklistId,
  title,
  items,
  taskId,
}: {
  checklistId: number;
  title: string;
  items: import('@/types/collab').ChecklistItem[];
  taskId: number;
}) {
  const queryClient = useQueryClient();
  const [adding, setAdding] = useState(false);
  const [content, setContent] = useState('');
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['checklists', taskId] });

  const toggle = useMutation({
    mutationFn: ({ id, completed }: { id: number; completed: boolean }) =>
      checklistsApi.toggleItem(id, completed),
    onSuccess: invalidate,
  });

  const addItem = useMutation({
    mutationFn: (c: string) => checklistsApi.addItem(checklistId, c),
    onSuccess: () => {
      invalidate();
      setContent('');
      setAdding(false);
    },
  });

  const removeItem = useMutation({
    mutationFn: (id: number) => checklistsApi.removeItem(id),
    onSuccess: invalidate,
  });

  const removeChecklist = useMutation({
    mutationFn: () => checklistsApi.remove(checklistId),
    onSuccess: invalidate,
  });

  return (
    <div>
      <div className="flex items-center justify-between mb-1.5">
        <h4 className="text-xs font-semibold text-gray-700 uppercase tracking-wide">{title}</h4>
        <button onClick={() => removeChecklist.mutate()} className="text-gray-300 hover:text-rose-500 text-xs">
          <X className="w-3.5 h-3.5" />
        </button>
      </div>
      <div className="space-y-1.5">
        {items
          .slice()
          .sort((a, b) => a.position - b.position)
          .map((it) => (
            <label key={it.id} className="flex items-center gap-2.5 text-sm group">
              <input
                type="checkbox"
                checked={it.completed}
                onChange={(e) => toggle.mutate({ id: it.id, completed: e.target.checked })}
                className="rounded text-emerald-500 focus:ring-emerald-500"
              />
              <span className={cn('flex-1', it.completed ? 'text-gray-400 line-through' : 'text-gray-700')}>
                {it.content}
              </span>
              <button
                onClick={() => removeItem.mutate(it.id)}
                className="opacity-0 group-hover:opacity-100 text-gray-300 hover:text-rose-500"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            </label>
          ))}
      </div>
      {adding ? (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            if (!content.trim()) return;
            addItem.mutate(content.trim());
          }}
          className="mt-2 flex gap-1.5"
        >
          <input
            value={content}
            onChange={(e) => setContent(e.target.value)}
            onKeyDown={(e) => e.key === 'Escape' && setAdding(false)}
            placeholder="Nội dung item…"
            autoFocus
            className="flex-1 px-2 py-1 text-sm border border-gray-300 rounded focus:ring-2 focus:ring-primary-500 outline-none"
          />
          <button type="submit" className="px-2 py-0.5 text-xs bg-primary-600 hover:bg-primary-700 text-white rounded">
            Lưu
          </button>
        </form>
      ) : (
        <button
          onClick={() => setAdding(true)}
          className="mt-2 text-xs text-gray-500 hover:text-primary-600 flex items-center gap-1"
        >
          <Plus className="w-3 h-3" /> Thêm item
        </button>
      )}
    </div>
  );
}
