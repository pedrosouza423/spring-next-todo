package com.springnexttodo.tasklist.dto;

import com.springnexttodo.tasklist.ListRole;
import jakarta.validation.constraints.NotNull;

public record InviteRequest(@NotNull ListRole role, String email) {}
