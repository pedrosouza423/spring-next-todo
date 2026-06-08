package com.springnexttodo.tasklist.dto;

import com.springnexttodo.tasklist.ListInvite;
import com.springnexttodo.tasklist.ListRole;

import java.time.Instant;

public record InviteResponse(String token, ListRole role, String listName, Instant expiresAt) {
    public static InviteResponse from(ListInvite invite) {
        return new InviteResponse(
                invite.getToken(),
                invite.getRole(),
                invite.getTaskList().getName(),
                invite.getExpiresAt()
        );
    }
}
