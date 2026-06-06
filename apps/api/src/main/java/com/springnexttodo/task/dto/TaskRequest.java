package com.springnexttodo.task.dto;

import com.springnexttodo.task.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaskRequest(
    @NotBlank(message = "title is required")
    @Size(max = 255, message = "title must be at most 255 characters")
    String title,

    @Size(max = 2000, message = "description must be at most 2000 characters")
    String description,

    Long categoryId,

    LocalDate dueDate,

    Priority priority
) {}
