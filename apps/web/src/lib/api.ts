const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export interface Task {
  id: number;
  title: string;
  description: string | null;
  completed: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ApiError {
  status: number;
  message: string;
  errors: string[];
  timestamp: string;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...init,
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error((body as ApiError).message ?? `HTTP ${res.status}`);
  }
  if (res.status === 204) return undefined as T;
  return res.json();
}

export const api = {
  tasks: {
    list: () => request<Task[]>("/tasks"),
    get: (id: number) => request<Task>(`/tasks/${id}`),
    create: (data: { title: string; description?: string }) =>
      request<Task>("/tasks", { method: "POST", body: JSON.stringify(data) }),
    update: (id: number, data: { title: string; description?: string }) =>
      request<Task>(`/tasks/${id}`, { method: "PUT", body: JSON.stringify(data) }),
    toggle: (id: number) =>
      request<Task>(`/tasks/${id}/toggle`, { method: "PATCH" }),
    delete: (id: number) =>
      request<void>(`/tasks/${id}`, { method: "DELETE" }),
  },
};