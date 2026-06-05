import { Task, User } from "@/lib/api";
import { TaskList } from "@/components/tasks/TaskList";
import { LogoutButton } from "@/components/auth/LogoutButton";
import { CheckSquare } from "lucide-react";
import { cookies } from "next/headers";

export const dynamic = "force-dynamic";

async function getTasks(authCookie: string | undefined): Promise<Task[]> {
  if (!authCookie) return [];
  try {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
    const res = await fetch(`${apiUrl}/tasks`, {
      headers: { Cookie: `auth_token=${authCookie}` },
      cache: "no-store",
    });
    return res.ok ? res.json() : [];
  } catch {
    return [];
  }
}

async function getCurrentUser(authCookie: string | undefined): Promise<User | null> {
  if (!authCookie) return null;
  try {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
    const res = await fetch(`${apiUrl}/auth/me`, {
      headers: { Cookie: `auth_token=${authCookie}` },
      cache: "no-store",
    });
    return res.ok ? res.json() : null;
  } catch {
    return null;
  }
}

export default async function HomePage() {
  const cookieStore = await cookies();
  const authCookie = cookieStore.get("auth_token")?.value;

  const [tasks, user] = await Promise.all([
    getTasks(authCookie),
    getCurrentUser(authCookie),
  ]);

  return (
    <main className="flex-1 flex flex-col">
      <header className="border-b border-border bg-card/50 backdrop-blur-sm sticky top-0 z-10">
        <div className="max-w-xl mx-auto px-4 py-4 flex items-center gap-2">
          <CheckSquare className="h-5 w-5 text-primary" />
          <h1 className="font-bold text-lg tracking-tight">spring-next-todo</h1>
          <span className="ml-auto text-sm text-muted-foreground">{user?.name}</span>
          <LogoutButton />
        </div>
      </header>

      <div className="flex-1 max-w-xl mx-auto w-full px-4 py-6">
        <TaskList initialTasks={tasks} />
      </div>
    </main>
  );
}