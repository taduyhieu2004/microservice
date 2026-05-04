import { Layout, MoreHorizontal } from 'lucide-react';
import { Link } from 'react-router-dom';
import { cn } from '@/lib/utils';
import type { Project, Role } from '@/types/project';

const ROLE_BADGE: Record<Role, string> = {
  OWNER: 'bg-primary-50 text-primary-700',
  ADMIN: 'bg-violet-50 text-violet-700',
  EDITOR: 'bg-emerald-50 text-emerald-700',
  COMMENTER: 'bg-sky-50 text-sky-700',
  VIEWER: 'bg-gray-100 text-gray-700',
};

const KEY_GRADIENTS = [
  'from-blue-500 to-indigo-600',
  'from-amber-500 to-orange-600',
  'from-emerald-500 to-teal-600',
  'from-violet-500 to-purple-600',
  'from-pink-500 to-rose-600',
  'from-cyan-500 to-sky-600',
];

function gradientFor(key: string) {
  let hash = 0;
  for (let i = 0; i < key.length; i++) hash = (hash * 31 + key.charCodeAt(i)) | 0;
  return KEY_GRADIENTS[Math.abs(hash) % KEY_GRADIENTS.length];
}

interface Props {
  project: Project;
}

export function ProjectCard({ project }: Props) {
  const role = project.my_role ?? 'VIEWER';
  return (
    <Link
      to={`/projects/${project.id}`}
      className="bg-white rounded-xl border border-gray-200 hover:shadow-md hover:-translate-y-0.5 transition p-5 group block"
    >
      <div className="flex items-start justify-between">
        <div
          className={cn(
            'w-10 h-10 rounded-lg bg-gradient-to-br flex items-center justify-center text-white font-bold text-sm',
            gradientFor(project.key),
          )}
        >
          {project.key.slice(0, 3)}
        </div>
        <button
          className="opacity-0 group-hover:opacity-100 p-1 text-gray-400 hover:text-gray-700 transition"
          onClick={(e) => e.preventDefault()}
        >
          <MoreHorizontal className="w-4 h-4" />
        </button>
      </div>
      <h3 className="mt-3 font-semibold text-gray-900 truncate">{project.name}</h3>
      <p className="text-sm text-gray-500 mt-1 line-clamp-2 min-h-[2.5rem]">
        {project.description ?? 'Không có mô tả'}
      </p>
      <div className="flex items-center justify-between mt-4 pt-4 border-t border-gray-100">
        <div className="flex items-center gap-1.5 text-xs text-gray-500">
          <Layout className="w-3.5 h-3.5" />
          {project.type}
        </div>
        <span className={cn('px-2 py-0.5 text-[11px] rounded font-semibold', ROLE_BADGE[role])}>{role}</span>
      </div>
    </Link>
  );
}
