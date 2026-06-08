package com.springnexttodo.tasklist.dto;

import com.springnexttodo.tasklist.ListRole;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(@NotNull ListRole role) {}
