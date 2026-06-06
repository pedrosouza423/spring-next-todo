"use client";

import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Task, Category, Priority, api } from "@/lib/api";
import { CategorySelector } from "./CategorySelector";
import { PrioritySelector } from "./PrioritySelector";

interface TaskEditDialogProps {
  task: Task;
  categories: Category[];
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSave: (task: Task) => void;
}

export function TaskEditDialog({ task, categories, open, onOpenChange, onSave }: TaskEditDialogProps) {
  const [title, setTitle] = useState(task.title);
  const [description, setDescription] = useState(task.description ?? "");
  const [categoryId, setCategoryId] = useState<number | null>(task.category?.id ?? null);
  const [dueDate, setDueDate] = useState(task.dueDate ?? "");
  const [priority, setPriority] = useState<Priority>(task.priority);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSave() {
    if (!title.trim()) { setError("Título obrigatório"); return; }
    setLoading(true);
    try {
      const updated = await api.tasks.update(task.id, {
        title: title.trim(),
        description: description.trim() || undefined,
        categoryId: categoryId,
        dueDate: dueDate || null,
        priority,
      });
      onSave(updated);
      onOpenChange(false);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Erro ao salvar");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Editar tarefa</DialogTitle>
        </DialogHeader>
        <div className="flex flex-col gap-3 py-2">
          <Input
            placeholder="Título *"
            value={title}
            onChange={(e) => { setTitle(e.target.value); setError(""); }}
            disabled={loading}
            autoFocus
          />
          <Input
            placeholder="Descrição (opcional)"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            disabled={loading}
          />
          {categories.length > 0 && (
            <CategorySelector
              categories={categories}
              value={categoryId}
              onChange={setCategoryId}
              disabled={loading}
            />
          )}
          <Input
            type="date"
            value={dueDate}
            onChange={(e) => setDueDate(e.target.value)}
            disabled={loading}
            aria-label="Data de vencimento"
          />
          <PrioritySelector value={priority} onChange={setPriority} disabled={loading} />
          {error && <p className="text-sm text-destructive">{error}</p>}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={loading}>Cancelar</Button>
          <Button onClick={handleSave} disabled={loading}>{loading ? "Salvando…" : "Salvar"}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
