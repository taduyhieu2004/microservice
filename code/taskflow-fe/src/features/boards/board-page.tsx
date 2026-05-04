import {
  closestCorners,
  DndContext,
  DragOverlay,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
  type DragStartEvent,
} from '@dnd-kit/core';
import { sortableKeyboardCoordinates } from '@dnd-kit/sortable';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ChevronRight, Filter, Plus, Star } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { BoardColumn } from '@/features/boards/board-column';
import { TaskCard } from '@/features/boards/task-card';
import { projectsApi } from '@/features/projects/projects-api';
import { labelsApi, tasksApi } from '@/features/tasks/tasks-api';
import { QuickCreateTask } from '@/features/tasks/quick-create-task';
import { extractErrorMessage } from '@/lib/api';
import type { BoardList } from '@/types/project';
import type { Task } from '@/types/task';

export function BoardPage() {
  const { projectId: projectIdParam } = useParams();
  const projectId = Number(projectIdParam);
  const queryClient = useQueryClient();

  const [activeTask, setActiveTask] = useState<Task | null>(null);
  const [creatingInList, setCreatingInList] = useState<number | null>(null);

  const projectQuery = useQuery({
    queryKey: ['project', projectId],
    queryFn: () => projectsApi.get(projectId),
    enabled: !!projectId,
  });

  const boardsQuery = useQuery({
    queryKey: ['boards', projectId],
    queryFn: () => projectsApi.boards(projectId),
    enabled: !!projectId,
  });

  const board = boardsQuery.data?.[0] ?? null;
  const boardId = board?.id;

  const tasksQuery = useQuery({
    queryKey: ['tasks', 'board', boardId],
    queryFn: () => tasksApi.list({ board_id: boardId!, size: 200 }),
    enabled: !!boardId,
  });

  const labelsQuery = useQuery({
    queryKey: ['labels', projectId],
    queryFn: () => labelsApi.list(projectId),
    enabled: !!projectId,
  });

  const tasksByList = useMemo(() => {
    const map = new Map<number, Task[]>();
    (tasksQuery.data?.content ?? []).forEach((t) => {
      const arr = map.get(t.list_id) ?? [];
      arr.push(t);
      map.set(t.list_id, arr);
    });
    map.forEach((arr) => arr.sort((a, b) => a.position - b.position));
    return map;
  }, [tasksQuery.data]);

  const moveMutation = useMutation({
    mutationFn: ({ id, toListId, position }: { id: number; toListId: number; position?: number }) =>
      tasksApi.move(id, { to_list_id: toListId, position }),
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['tasks', 'board', boardId] }),
  });

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  );

  function onDragStart(e: DragStartEvent) {
    const t = (e.active.data.current as { type?: string; task?: Task } | undefined)?.task;
    if (t) setActiveTask(t);
  }

  function onDragEnd(e: DragEndEvent) {
    setActiveTask(null);
    const { active, over } = e;
    if (!over) return;

    const activeData = active.data.current as { type?: string; task?: Task } | undefined;
    const overData = over.data.current as { type?: string; task?: Task; listId?: number } | undefined;
    const draggedTask = activeData?.task;
    if (!draggedTask) return;

    let toListId: number | undefined;
    let position: number | undefined;

    if (overData?.type === 'column') {
      toListId = overData.listId;
      position = (tasksByList.get(toListId!)?.length ?? 0);
    } else if (overData?.type === 'task' && overData.task) {
      toListId = overData.task.list_id;
      const list = tasksByList.get(toListId) ?? [];
      const idx = list.findIndex((t) => t.id === overData.task!.id);
      position = idx >= 0 ? idx : list.length;
    }

    if (toListId == null) return;
    if (toListId === draggedTask.list_id && position === draggedTask.position) return;

    queryClient.setQueryData(
      ['tasks', 'board', boardId],
      (old: { content?: Task[] } | undefined) => {
        if (!old?.content) return old;
        const updated = old.content.map((t) =>
          t.id === draggedTask.id ? { ...t, list_id: toListId!, position: position ?? 0 } : t,
        );
        return { ...old, content: updated };
      },
    );

    moveMutation.mutate({ id: draggedTask.id, toListId, position });
  }

  const project = projectQuery.data;
  const lists: BoardList[] = (board?.lists ?? []).slice().sort((a, b) => a.position - b.position);
  const labels = labelsQuery.data ?? [];

  return (
    <div className="flex flex-col h-[calc(100vh-3.5rem)]">
      <div className="bg-white border-b border-gray-200 px-6 py-4 flex-shrink-0">
        <div className="flex items-center gap-2 text-sm text-gray-500">
          <Link to="/dashboard" className="hover:text-gray-700">Projects</Link>
          <ChevronRight className="w-3.5 h-3.5" />
          <span className="text-gray-700">{project?.name ?? '...'}</span>
          <ChevronRight className="w-3.5 h-3.5" />
          <span className="text-gray-900 font-medium">{board?.name ?? '...'}</span>
        </div>
        <div className="flex items-center justify-between mt-2">
          <div className="flex items-center gap-3">
            <h1 className="text-xl font-bold text-gray-900">{board?.name ?? 'Board'}</h1>
            <button className="p-1 text-gray-400 hover:text-amber-500"><Star className="w-4 h-4" /></button>
          </div>
          <div className="flex items-center gap-3">
            <button className="px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-100 rounded-lg flex items-center gap-1.5">
              <Filter className="w-4 h-4" /> Lọc
            </button>
            <Link
              to={`/projects/${projectId}/members`}
              className="px-3 py-1.5 text-sm text-primary-600 hover:bg-primary-50 rounded-lg"
            >
              Members
            </Link>
          </div>
        </div>
      </div>

      {boardsQuery.isError && (
        <div className="m-6 text-sm text-rose-600 bg-rose-50 border border-rose-200 rounded-lg p-3">
          {extractErrorMessage(boardsQuery.error)}
        </div>
      )}

      {board && (
        <DndContext sensors={sensors} collisionDetection={closestCorners} onDragStart={onDragStart} onDragEnd={onDragEnd}>
          <div className="flex-1 overflow-x-auto overflow-y-hidden p-6 scrollbar-thin">
            <div className="flex gap-4 h-full min-w-max items-start">
              {lists.map((list) => {
                const listTasks = tasksByList.get(list.id) ?? [];
                return (
                  <div key={list.id} className="h-full">
                    <BoardColumn
                      list={list}
                      tasks={listTasks}
                      labels={labels}
                      onAddTask={() => setCreatingInList(list.id)}
                    />
                    {creatingInList === list.id && boardId && (
                      <div className="-mt-1">
                        <QuickCreateTask listId={list.id} boardId={boardId} onClose={() => setCreatingInList(null)} />
                      </div>
                    )}
                  </div>
                );
              })}

              <button className="w-72 h-32 rounded-xl border-2 border-dashed border-gray-300 hover:border-primary-500 hover:bg-primary-50/30 transition flex items-center justify-center text-gray-500 hover:text-primary-600 font-medium text-sm flex-shrink-0">
                <Plus className="w-4 h-4 mr-1" /> Thêm cột
              </button>
            </div>
          </div>

          <DragOverlay>
            {activeTask ? <TaskCard task={activeTask} labels={labels} /> : null}
          </DragOverlay>
        </DndContext>
      )}
    </div>
  );
}
