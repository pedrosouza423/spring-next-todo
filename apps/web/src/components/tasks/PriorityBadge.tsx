"use client";

import { Badge } from "@/components/ui/badge";
import { Flag } from "lucide-react";
import { Priority } from "@/lib/api";

interface PriorityBadgeProps {
  priority: Priority;
}

const PRIORITY_LABELS: Record<Priority, string> = {
  LOW: "Baixa",
  MEDIUM: "Média",
  HIGH: "Alta",
};

const PRIORITY_CLASSES: Record<Priority, string> = {
  LOW: "border-border text-muted-foreground",
  MEDIUM: "border-amber-500 text-amber-500",
  HIGH: "",
};

export function PriorityBadge({ priority }: PriorityBadgeProps) {
  return (
    <Badge
      variant={priority === "HIGH" ? "destructive" : "outline"}
      className={`text-xs gap-1.5 font-normal ${PRIORITY_CLASSES[priority]}`}
    >
      <Flag className="h-3 w-3 shrink-0" />
      {PRIORITY_LABELS[priority]}
    </Badge>
  );
}
