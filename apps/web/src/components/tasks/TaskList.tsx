"use client";

import { useState, useEffect, useRef } from "react";
import { Task, Category, Priority, api } from "@/lib/api";
import { TaskItem } from "./TaskItem";
import { TaskForm } from "./TaskForm";
import { PRIORITY_LABELS } from "./PriorityBadge";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

interface TaskListProps {
  initialTasks: Task[];
  categories: Category[];
}

const PRIORITIES: Priority[] = ["LOW", "MEDIUM", "HIGH"];

export function TaskList({ initialTasks, categories }: TaskListProps) {
  const [tasks, setTasks] = useState<Task[]>(initialTasks);
  const [filterCategoryId, setFilterCategoryId] = useState<number | null>(null);
  const [filterPriority, setFilterPriority] = useState<Priority | null>(null);
  const [filterCompleted, setFilterCompleted] = useState<boolean | null>(null);
  const [sortBy, setSortBy] = useState<"dueDate" | "createdAt">("dueDate");
  const mounted = useRef(false);

  useEffect(() => {
    if (!mounted.current) {
      mounted.current = true;
      return;
    }
    let cancelled = false;
    api.tasks.list({
      categoryId: filterCategoryId ?? undefined,
      priority: filterPriority ?? undefined,
      completed: filterCompleted ?? undefined,
    })
      .then((data) => { if (!cancelled) setTasks(data); })
      .catch((err) => { if (!cancelled) console.error(err); });
    return () => { cancelled = true; };
  }, [filterCategoryId, filterPriority, filterCompleted]);

  function matchesFilters(t: Task) {
    return (
      (filterPriority === null || t.priority === filterPriority) &&
      (filterCompleted === null || t.completed === filterCompleted) &&
      (filterCategoryId === null || t.category?.id === filterCategoryId)
    );
  }

  function handleCreated(task: Task) {
    if (matchesFilters(task)) setTasks((prev) => [task, ...prev]);
  }

  function handleUpdate(updated: Task) {
    setTasks((prev) =>
      matchesFilters(updated)
        ? prev.map((t) => (t.id === updated.id ? updated : t))
        : prev.filter((t) => t.id !== updated.id)
    );
  }

  function handleDelete(id: number) {
    setTasks((prev) => prev.filter((t) => t.id !== id));
  }

  const sorted = tasks.slice().sort((a, b) => {
    if (sortBy === "dueDate") {
      if (a.dueDate && b.dueDate && a.dueDate !== b.dueDate)
        return a.dueDate < b.dueDate ? -1 : 1;
      if (a.dueDate && !b.dueDate) return -1;
      if (!a.dueDate && b.dueDate) return 1;
    }
    return b.createdAt < a.createdAt ? -1 : b.createdAt > a.createdAt ? 1 : 0;
  });

  const pending = sorted.filter((t) => !t.completed);
  const done = sorted.filter((t) => t.completed);

  const anyFilter = filterCategoryId != null || filterPriority != null || filterCompleted != null;

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

      {/* Category filter */}
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

      {/* Priority filter */}
      <div className="flex flex-wrap gap-2">
        <button
          type="button"
          onClick={() => setFilterPriority(null)}
          className={cn(
            "px-3 py-1 text-xs rounded-full border transition-colors",
            filterPriority === null
              ? "bg-primary text-primary-foreground border-primary"
              : "border-border text-muted-foreground hover:text-foreground"
          )}
        >
          Prioridade: Todas
        </button>
        {PRIORITIES.map((p) => (
          <button
            key={p}
            type="button"
            onClick={() => setFilterPriority(p === filterPriority ? null : p)}
            className={cn(
              "px-3 py-1 text-xs rounded-full border transition-colors",
              filterPriority === p
                ? "bg-primary text-primary-foreground border-primary"
                : "border-border text-muted-foreground hover:text-foreground"
            )}
          >
            {PRIORITY_LABELS[p]}
          </button>
        ))}
      </div>

      {/* Status filter */}
      <div className="flex flex-wrap gap-2">
        {([null, false, true] as const).map((val) => {
          const label = val === null ? "Status: Todas" : val ? "Concluídas" : "Pendentes";
          return (
            <button
              key={String(val)}
              type="button"
              onClick={() => setFilterCompleted(val === filterCompleted ? null : val)}
              className={cn(
                "px-3 py-1 text-xs rounded-full border transition-colors",
                filterCompleted === val
                  ? "bg-primary text-primary-foreground border-primary"
                  : "border-border text-muted-foreground hover:text-foreground"
              )}
            >
              {label}
            </button>
          );
        })}
      </div>

      {sorted.length === 0 && (
        <p className="text-center text-muted-foreground text-sm py-8">
          {anyFilter
            ? "Nenhuma tarefa encontrada com esses filtros."
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
