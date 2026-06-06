package com.springnexttodo.task.dto;

import com.springnexttodo.category.dto.CategoryResponse;
import com.springnexttodo.task.Task;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
    Long id,
    String title,
    String description,
    boolean completed,
    Instant createdAt,
    Instant updatedAt,
    CategoryResponse category,
    LocalDate dueDate
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.isCompleted(),
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getCategory() != null ? CategoryResponse.from(task.getCategory()) : null,
            task.getDueDate()
        );
    }
}
