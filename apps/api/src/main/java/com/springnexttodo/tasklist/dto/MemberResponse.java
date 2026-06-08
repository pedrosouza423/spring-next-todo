package com.springnexttodo.tasklist.dto;

import com.springnexttodo.tasklist.ListRole;
import com.springnexttodo.tasklist.TaskListMember;

public record MemberResponse(Long userId, String name, String email, ListRole role) {
    public static MemberResponse from(TaskListMember m) {
        return new MemberResponse(
                m.getUser().getId(),
                m.getUser().getName(),
                m.getUser().getEmail(),
                m.getRole()
        );
    }
}
