package com.springnexttodo.tasklist;

import com.springnexttodo.auth.User;
import com.springnexttodo.common.ForbiddenException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ListAccessService {

    private final TaskListMemberRepository memberRepository;

    public ListAccessService(TaskListMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public TaskListMember requireMembership(TaskList list, User user) {
        return memberRepository.findByTaskListAndUser(list, user)
                .orElseThrow(() -> new EntityNotFoundException("Task list not found: " + list.getId()));
    }

    public TaskListMember requireRole(TaskList list, User user, ListRole minRole) {
        TaskListMember member = requireMembership(list, user);
        if (!member.getRole().atLeast(minRole)) {
            throw new ForbiddenException("Insufficient role for this operation");
        }
        return member;
    }
}
