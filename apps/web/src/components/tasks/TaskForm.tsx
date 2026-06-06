"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Plus } from "lucide-react";
import { Task, Category, api } from "@/lib/api";
import { CategorySelector } from "./CategorySelector";

interface TaskFormProps {
  onCreated: (task: Task) => void;
  categories: Category[];
}

export function TaskForm({ onCreated, categories }: TaskFormProps) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [dueDate, setDueDate] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [expanded, setExpanded] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!title.trim()) { setError("Título obrigatório"); return; }
    setLoading(true);
    try {
      const task = await api.tasks.create({
        title: title.trim(),
        description: description.trim() || undefined,
        categoryId: categoryId ?? undefined,
        dueDate: dueDate || undefined,
      });
      onCreated(task);
      setTitle("");
      setDescription("");
      setCategoryId(null);
      setDueDate("");
      setExpanded(false);
      setError("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao criar tarefa");
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-2">
      <div className="flex gap-2">
        <Input
          placeholder="Nova tarefa…"
          value={title}
          onChange={(e) => { setTitle(e.target.value); setError(""); if (!expanded && e.target.value) setExpanded(true); }}
          disabled={loading}
          className="flex-1"
          aria-label="Título da tarefa"
        />
        <Button type="submit" disabled={loading || !title.trim()} size="icon" aria-label="Adicionar tarefa">
          <Plus className="h-4 w-4" />
        </Button>
      </div>

      {expanded && (
        <>
          <Input
            placeholder="Descrição (opcional)"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            disabled={loading}
            aria-label="Descrição da tarefa"
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
        </>
      )}

      {error && <p className="text-sm text-destructive">{error}</p>}
    </form>
  );
}
