"use client";

import { Priority } from "@/lib/api";

interface PrioritySelectorProps {
  value: Priority;
  onChange: (priority: Priority) => void;
  disabled?: boolean;
}

export function PrioritySelector({ value, onChange, disabled }: PrioritySelectorProps) {
  return (
    <select
      className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
      value={value}
      onChange={(e) => onChange(e.target.value as Priority)}
      disabled={disabled}
      aria-label="Prioridade"
    >
      <option value="LOW">Prioridade: Baixa</option>
      <option value="MEDIUM">Prioridade: Média</option>
      <option value="HIGH">Prioridade: Alta</option>
    </select>
  );
}
