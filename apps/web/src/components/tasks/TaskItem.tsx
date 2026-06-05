"use client";

import { useState } from "react";
import { Checkbox } from "@/components/ui/checkbox";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Pencil, Trash2 } from "lucide-react";
import { Task, Category, api } from "@/lib/api";
import { TaskEditDialog } from "./TaskEditDialog";
import { CategoryBadge } from "./CategoryBadge";
import { cn } from "@/lib/utils";

interface TaskItemProps {
  task: Task;
  categories: Category[];
  onUpdate: (task: Task) => void;
  onDelete: (id: number) => void;
}

export function TaskItem({ task, categories, onUpdate, onDelete }: TaskItemProps) {
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(false);

  async function handleToggle() {
    setLoading(true);
    try {
      const updated = await api.tasks.toggle(task.id);
      onUpdate(updated);
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete() {
    setLoading(true);
    try {
      await api.tasks.delete(task.id);
      onDelete(task.id);
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <div className="flex items-start gap-3 p-4 rounded-xl border border-border bg-card hover:bg-card/80 transition-colors">
        <Checkbox
          checked={task.completed}
          onCheckedChange={handleToggle}
          disabled={loading}
          className="mt-0.5 shrink-0"
          aria-label={task.completed ? "Marcar como pendente" : "Marcar como concluída"}
        />

        <div className="flex-1 min-w-0">
          <p className={cn("font-medium leading-snug break-words", task.completed && "line-through text-muted-foreground")}>
            {task.title}
          </p>
          {task.description && (
            <p className="text-sm text-muted-foreground mt-0.5 break-words">{task.description}</p>
          )}
          {task.category && (
            <div className="mt-1.5">
              <CategoryBadge category={task.category} />
            </div>
          )}
        </div>

        <div className="flex items-center gap-1 shrink-0">
          {task.completed && (
            <Badge variant="secondary" className="text-xs hidden sm:inline-flex">Concluída</Badge>
          )}
          <Button
            variant="ghost"
            size="icon"
            className="h-8 w-8 text-muted-foreground hover:text-foreground"
            onClick={() => setEditing(true)}
            disabled={loading}
            aria-label="Editar"
          >
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            className="h-8 w-8 text-muted-foreground hover:text-destructive"
            onClick={handleDelete}
            disabled={loading}
            aria-label="Deletar"
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      </div>

      <TaskEditDialog
        key={task.id}
        task={task}
        categories={categories}
        open={editing}
        onOpenChange={setEditing}
        onSave={onUpdate}
      />
    </>
  );
}
