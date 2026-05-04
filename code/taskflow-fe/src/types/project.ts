export type ProjectType = 'SOFTWARE' | 'BUSINESS' | 'PERSONAL';
export type Role = 'OWNER' | 'ADMIN' | 'EDITOR' | 'COMMENTER' | 'VIEWER';

export interface Project {
  id: number;
  name: string;
  key: string;
  description?: string | null;
  type: ProjectType;
  owner_id: number;
  my_role?: Role | null;
  created_at: number;
}

export interface CreateProjectRequest {
  name: string;
  key: string;
  description?: string;
  type?: ProjectType;
}

export interface UpdateProjectRequest {
  name?: string;
  description?: string;
  type?: ProjectType;
}

export interface BoardList {
  id: number;
  board_id: number;
  name: string;
  description?: string | null;
  position: number;
}

export interface Board {
  id: number;
  project_id: number;
  name: string;
  description?: string | null;
  color?: string | null;
  position: number;
  lists?: BoardList[];
}

export interface Member {
  id: number;
  project_id: number;
  user_id: number;
  role: Role;
  joined_at: number;
}
