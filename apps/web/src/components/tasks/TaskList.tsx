"use client";

import { useState } from "react";
import { Task, Category } from "@/lib/api";
import { TaskItem } from "./TaskItem";
import { TaskForm } from "./TaskForm";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

interface TaskListProps {
  initialTasks: Task[];
  categories: Category[];
}

export function TaskList({ initialTasks, categories }: TaskListProps) {
  const [tasks, setTasks] = useState<Task[]>(initialTasks);
  const [filterCategoryId, setFilterCategoryId] = useState<number | null>(null);
  const [sortBy, setSortBy] = useState<"dueDate" | "createdAt">("dueDate");

  function handleCreated(task: Task) {
    setTasks((prev) => [task, ...prev]);
  }

  function handleUpdate(updated: Task) {
    setTasks((prev) => prev.map((t) => (t.id === updated.id ? updated : t)));
  }

  function handleDelete(id: number) {
    setTasks((prev) => prev.filter((t) => t.id !== id));
  }

  const filtered = (filterCategoryId != null
    ? tasks.filter((t) => t.category?.id === filterCategoryId)
    : tasks
  ).slice().sort((a, b) => {
    if (sortBy === "dueDate") {
      if (a.dueDate && b.dueDate) return a.dueDate < b.dueDate ? -1 : a.dueDate > b.dueDate ? 1 : 0;
      if (a.dueDate) return -1;
      if (b.dueDate) return 1;
    }
    return b.createdAt < a.createdAt ? -1 : b.createdAt > a.createdAt ? 1 : 0;
  });

  const pending = filtered.filter((t) => !t.completed);
  const done = filtered.filter((t) => t.completed);

  return (
    <div className="flex flex-col gap-6">
      <TaskForm onCreated={handleCreated} categories={categories} />

      <div className="flex items-center justify-end">
        <select
          className="flex h-8 rounded-md border border-input bg-transparent px-2 py-1 text-xs shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
          value={sortBy}
          onChange={(e) => setSortBy(e.target.value as "dueDate" | "createdAt")}
          aria-label="Ordenar por"
        >
          <option value="dueDate">Ordenar: Prazo</option>
          <option value="createdAt">Ordenar: Mais recentes</option>
        </select>
      </div>

      {categories.length > 0 && (
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            onClick={() => setFilterCategoryId(null)}
            className={cn(
              "px-3 py-1 text-xs rounded-full border transition-colors",
              filterCategoryId === null
                ? "bg-primary text-primary-foreground border-primary"
                : "border-border text-muted-foreground hover:text-foreground"
            )}
          >
            Todas
          </button>
          {categories.map((cat) => (
            <button
              key={cat.id}
              type="button"
              onClick={() => setFilterCategoryId(cat.id === filterCategoryId ? null : cat.id)}
              className={cn(
                "px-3 py-1 text-xs rounded-full border transition-colors",
                filterCategoryId === cat.id
                  ? "text-white border-transparent"
                  : "border-border text-muted-foreground hover:text-foreground"
              )}
              style={filterCategoryId === cat.id ? { backgroundColor: cat.color, borderColor: cat.color } : {}}
            >
              {cat.name}
            </button>
          ))}
        </div>
      )}

      {filtered.length === 0 && (
        <p className="text-center text-muted-foreground text-sm py-8">
          {filterCategoryId != null
            ? "Nenhuma tarefa nesta categoria."
            : "Nenhuma tarefa ainda. Adicione a primeira acima!"}
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
              <TaskItem key={task.id} task={task} categories={categories} onUpdate={handleUpdate} onDelete={handleDelete} />
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
              <TaskItem key={task.id} task={task} categories={categories} onUpdate={handleUpdate} onDelete={handleDelete} />
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
