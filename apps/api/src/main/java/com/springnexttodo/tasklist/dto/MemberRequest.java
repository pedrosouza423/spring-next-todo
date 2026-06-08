package com.springnexttodo.tasklist.dto;

import com.springnexttodo.tasklist.ListRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberRequest(
        @Email @NotBlank String email,
        @NotNull ListRole role
) {}
