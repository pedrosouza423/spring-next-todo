package com.springnexttodo.tasklist.dto;

import com.springnexttodo.tasklist.ListRole;
import com.springnexttodo.tasklist.TaskList;

import java.time.Instant;

public record TaskListResponse(
        Long id,
        String name,
        ListRole role,
        int memberCount,
        Instant createdAt
) {
    public static TaskListResponse from(TaskList list, ListRole role, int memberCount) {
        return new TaskListResponse(
                list.getId(),
                list.getName(),
                role,
                memberCount,
                list.getCreatedAt()
        );
    }
}
