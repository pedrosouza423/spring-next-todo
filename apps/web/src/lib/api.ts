const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export interface Category {
  id: number;
  name: string;
  color: string;
}

export interface Task {
  id: number;
  title: string;
  description: string | null;
  completed: boolean;
  createdAt: string;
  updatedAt: string;
  category: Category | null;
}

export interface User {
  id: number;
  name: string;
  email: string;
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
    credentials: "include",
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
    create: (data: { title: string; description?: string; categoryId?: number }) =>
      request<Task>("/tasks", { method: "POST", body: JSON.stringify(data) }),
    update: (id: number, data: { title: string; description?: string; categoryId?: number | null }) =>
      request<Task>(`/tasks/${id}`, { method: "PUT", body: JSON.stringify(data) }),
    toggle: (id: number) =>
      request<Task>(`/tasks/${id}/toggle`, { method: "PATCH" }),
    delete: (id: number) =>
      request<void>(`/tasks/${id}`, { method: "DELETE" }),
  },
  auth: {
    register: (data: { name: string; email: string; password: string }) =>
      request<User>("/auth/register", { method: "POST", body: JSON.stringify(data) }),
    login: (data: { email: string; password: string }) =>
      request<User>("/auth/login", { method: "POST", body: JSON.stringify(data) }),
    logout: () =>
      request<void>("/auth/logout", { method: "POST" }),
    me: () =>
      request<User>("/auth/me"),
  },
  categories: {
    list: () => request<Category[]>("/categories"),
    create: (data: { name: string; color: string }) =>
      request<Category>("/categories", { method: "POST", body: JSON.stringify(data) }),
    update: (id: number, data: { name: string; color: string }) =>
      request<Category>(`/categories/${id}`, { method: "PUT", body: JSON.stringify(data) }),
    delete: (id: number) =>
      request<void>(`/categories/${id}`, { method: "DELETE" }),
  },
};
