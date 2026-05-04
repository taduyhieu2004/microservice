export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface Task {
  id: number;
  project_id: number;
  board_id: number;
  list_id: number;
  sprint_id?: number | null;
  title: string;
  description?: string | null;
  assignee_id?: number | null;
  reporter_id?: number | null;
  due_date?: number | null;
  priority?: Priority | null;
  position: number;
  version: number;
  created_at: number;
  last_updated_at: number;
  label_ids?: number[];
}

export interface CreateTaskRequest {
  list_id: number;
  title: string;
  description?: string;
  assignee_id?: number;
  due_date?: number;
  priority?: Priority;
  label_ids?: number[];
  sprint_id?: number;
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string;
  assignee_id?: number | null;
  due_date?: number | null;
  priority?: Priority | null;
  label_ids?: number[];
  sprint_id?: number | null;
  version: number;
}

export interface MoveTaskRequest {
  to_list_id: number;
  position?: number;
}

export interface Label {
  id: number;
  project_id: number;
  name: string;
  color: string;
}

export interface CreateLabelRequest {
  project_id: number;
  name: string;
  color: string;
}
