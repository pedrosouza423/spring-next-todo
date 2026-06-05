"use client";

import { Badge } from "@/components/ui/badge";
import { Category } from "@/lib/api";

interface CategoryBadgeProps {
  category: Category;
}

export function CategoryBadge({ category }: CategoryBadgeProps) {
  return (
    <Badge
      variant="outline"
      className="text-xs gap-1.5 font-normal"
      style={{ borderColor: category.color, color: category.color }}
    >
      <span
        className="inline-block h-2 w-2 rounded-full shrink-0"
        style={{ backgroundColor: category.color }}
      />
      {category.name}
    </Badge>
  );
}
