"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Plus } from "lucide-react";
import { Task, api } from "@/lib/api";

interface TaskFormProps {
  onCreated: (task: Task) => void;
}

export function TaskForm({ onCreated }: TaskFormProps) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [expanded, setExpanded] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!title.trim()) { setError("Título obrigatório"); return; }
    setLoading(true);
    try {
      const task = await api.tasks.create({ title: title.trim(), description: description.trim() || undefined });
      onCreated(task);
      setTitle("");
      setDescription("");
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
        <Input
          placeholder="Descrição (opcional)"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          disabled={loading}
          aria-label="Descrição da tarefa"
        />
      )}

      {error && <p className="text-sm text-destructive">{error}</p>}
    </form>
  );
}