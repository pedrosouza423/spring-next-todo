"use client";

import { Badge } from "@/components/ui/badge";
import { CalendarDays } from "lucide-react";

interface DueDateBadgeProps {
  dueDate: string;
  completed: boolean;
}

function formatDate(dateStr: string): string {
  return new Date(dateStr + "T00:00:00").toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "short",
  });
}

function isOverdue(dateStr: string): boolean {
  const now = new Date();
  const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
  return dateStr < today;
}

export function DueDateBadge({ dueDate, completed }: DueDateBadgeProps) {
  const overdue = !completed && isOverdue(dueDate);
  const label = formatDate(dueDate);

  return (
    <Badge
      variant={overdue ? "destructive" : "outline"}
      className="text-xs gap-1.5 font-normal"
    >
      <CalendarDays className="h-3 w-3 shrink-0" />
      {overdue ? `Vencida · ${label}` : label}
    </Badge>
  );
}
