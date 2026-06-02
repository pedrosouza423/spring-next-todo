import { api, Task } from "@/lib/api";
import { TaskList } from "@/components/tasks/TaskList";
import { CheckSquare } from "lucide-react";

export const dynamic = "force-dynamic";

async function getTasks(): Promise<Task[]> {
  try {
    return await api.tasks.list();
  } catch {
    return [];
  }
}

export default async function HomePage() {
  const tasks = await getTasks();

  return (
    <main className="flex-1 flex flex-col">
      <header className="border-b border-border bg-card/50 backdrop-blur-sm sticky top-0 z-10">
        <div className="max-w-xl mx-auto px-4 py-4 flex items-center gap-2">
          <CheckSquare className="h-5 w-5 text-primary" />
          <h1 className="font-bold text-lg tracking-tight">spring-next-todo</h1>
        </div>
      </header>

      <div className="flex-1 max-w-xl mx-auto w-full px-4 py-6">
        <TaskList initialTasks={tasks} />
      </div>
    </main>
  );
}