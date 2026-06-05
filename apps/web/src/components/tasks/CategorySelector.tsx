"use client";

import { Category } from "@/lib/api";

interface CategorySelectorProps {
  categories: Category[];
  value: number | null;
  onChange: (categoryId: number | null) => void;
  disabled?: boolean;
}

export function CategorySelector({ categories, value, onChange, disabled }: CategorySelectorProps) {
  return (
    <select
      className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
      value={value ?? ""}
      onChange={(e) => onChange(e.target.value ? Number(e.target.value) : null)}
      disabled={disabled}
      aria-label="Categoria"
    >
      <option value="">Sem categoria</option>
      {categories.map((cat) => (
        <option key={cat.id} value={cat.id}>
          {cat.name}
        </option>
      ))}
    </select>
  );
}
