package com.springnexttodo.tasklist;

import com.springnexttodo.auth.User;
import com.springnexttodo.auth.UserRepository;
import com.springnexttodo.common.ForbiddenException;
import com.springnexttodo.tasklist.dto.MemberRequest;
import com.springnexttodo.tasklist.dto.MemberResponse;
import com.springnexttodo.tasklist.dto.RoleUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final TaskListRepository listRepository;
    private final TaskListMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ListAccessService accessService;

    public MemberService(TaskListRepository listRepository,
                         TaskListMemberRepository memberRepository,
                         UserRepository userRepository,
                         ListAccessService accessService) {
        this.listRepository = listRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.accessService = accessService;
    }

    public List<MemberResponse> listMembers(Long listId, User requester) {
        TaskList list = getListOrThrow(listId);
        accessService.requireRole(list, requester, ListRole.VIEWER);
        return memberRepository.findByTaskList(list).stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional
    public MemberResponse addMember(Long listId, MemberRequest req, User requester) {
        TaskList list = getListOrThrow(listId);
        accessService.requireRole(list, requester, ListRole.OWNER);

        User target = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + req.email()));

        if (memberRepository.existsByTaskListAndUser(list, target)) {
            throw new IllegalStateException("User is already a member of this list");
        }

        var membership = new TaskListMember();
        membership.setTaskList(list);
        membership.setUser(target);
        membership.setRole(req.role());
        return MemberResponse.from(memberRepository.save(membership));
    }

    @Transactional
    public MemberResponse updateRole(Long listId, Long userId, RoleUpdateRequest req, User requester) {
        TaskList list = getListOrThrow(listId);
        accessService.requireRole(list, requester, ListRole.OWNER);

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        TaskListMember membership = memberRepository.findByTaskListAndUser(list, target)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        if (membership.getRole() == ListRole.OWNER) {
            throw new ForbiddenException("Cannot change the role of the list owner");
        }

        membership.setRole(req.role());
        return MemberResponse.from(memberRepository.save(membership));
    }

    @Transactional
    public void removeMember(Long listId, Long userId, User requester) {
        TaskList list = getListOrThrow(listId);

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        boolean isSelf = requester.getEmail().equals(target.getEmail());
        if (isSelf) {
            accessService.requireMembership(list, requester);
        } else {
            accessService.requireRole(list, requester, ListRole.OWNER);
        }

        TaskListMember membership = memberRepository.findByTaskListAndUser(list, target)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        memberRepository.delete(membership);
    }

    private TaskList getListOrThrow(Long id) {
        return listRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task list not found: " + id));
    }
}
