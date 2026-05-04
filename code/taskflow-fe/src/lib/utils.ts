import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

const AVATAR_GRADIENTS = [
  'from-pink-400 to-rose-500',
  'from-emerald-400 to-teal-500',
  'from-violet-400 to-purple-500',
  'from-blue-400 to-indigo-500',
  'from-amber-400 to-orange-500',
  'from-cyan-400 to-sky-500',
  'from-fuchsia-400 to-pink-500',
];

export function avatarGradient(seed: string | number) {
  const key = String(seed ?? '');
  let hash = 0;
  for (let i = 0; i < key.length; i++) hash = (hash * 31 + key.charCodeAt(i)) | 0;
  return AVATAR_GRADIENTS[Math.abs(hash) % AVATAR_GRADIENTS.length];
}

export function initials(name?: string | null) {
  if (!name) return '?';
  const trimmed = name.trim();
  if (!trimmed) return '?';
  const parts = trimmed.split(/\s+/);
  if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
  return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
}

export function formatRelativeTime(epochMillis: number | null | undefined) {
  if (!epochMillis) return '';
  const diff = Date.now() - epochMillis;
  const sec = Math.floor(diff / 1000);
  if (sec < 60) return 'vừa xong';
  const min = Math.floor(sec / 60);
  if (min < 60) return `${min} phút trước`;
  const hour = Math.floor(min / 60);
  if (hour < 24) return `${hour} giờ trước`;
  const day = Math.floor(hour / 24);
  if (day < 7) return `${day} ngày trước`;
  return new Date(epochMillis).toLocaleDateString('vi-VN');
}
