"use client";

import { useState } from "react";
import { Task } from "@/lib/api";
import { TaskItem } from "./TaskItem";
import { TaskForm } from "./TaskForm";
import { Badge } from "@/components/ui/badge";

interface TaskListProps {
  initialTasks: Task[];
}

export function TaskList({ initialTasks }: TaskListProps) {
  const [tasks, setTasks] = useState<Task[]>(initialTasks);

  function handleCreated(task: Task) {
    setTasks((prev) => [task, ...prev]);
  }

  function handleUpdate(updated: Task) {
    setTasks((prev) => prev.map((t) => (t.id === updated.id ? updated : t)));
  }

  function handleDelete(id: number) {
    setTasks((prev) => prev.filter((t) => t.id !== id));
  }

  const pending = tasks.filter((t) => !t.completed);
  const done = tasks.filter((t) => t.completed);

  return (
    <div className="flex flex-col gap-6">
      <TaskForm onCreated={handleCreated} />

      {tasks.length === 0 && (
        <p className="text-center text-muted-foreground text-sm py-8">
          Nenhuma tarefa ainda. Adicione a primeira acima!
        </p>
      )}

      {pending.length > 0 && (
        <section>
          <div className="flex items-center gap-2 mb-3">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">Pendentes</h2>
            <Badge variant="outline">{pending.length}</Badge>
          </div>
          <div className="flex flex-col gap-2">
            {pending.map((task) => (
              <TaskItem key={task.id} task={task} onUpdate={handleUpdate} onDelete={handleDelete} />
            ))}
          </div>
        </section>
      )}

      {done.length > 0 && (
        <section>
          <div className="flex items-center gap-2 mb-3">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">Concluídas</h2>
            <Badge variant="outline">{done.length}</Badge>
          </div>
          <div className="flex flex-col gap-2">
            {done.map((task) => (
              <TaskItem key={task.id} task={task} onUpdate={handleUpdate} onDelete={handleDelete} />
            ))}
          </div>
        </section>
      )}
    </div>
  );
}