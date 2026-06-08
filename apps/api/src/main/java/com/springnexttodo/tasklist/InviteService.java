package com.springnexttodo.tasklist;

import com.springnexttodo.auth.User;
import com.springnexttodo.tasklist.dto.InviteRequest;
import com.springnexttodo.tasklist.dto.InviteResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
@Transactional(readOnly = true)
public class InviteService {

    private static final int TOKEN_BYTES = 24;
    private static final int EXPIRY_DAYS = 7;

    private final TaskListRepository listRepository;
    private final TaskListMemberRepository memberRepository;
    private final ListInviteRepository inviteRepository;
    private final ListAccessService accessService;
    private final SecureRandom secureRandom = new SecureRandom();

    public InviteService(TaskListRepository listRepository,
                         TaskListMemberRepository memberRepository,
                         ListInviteRepository inviteRepository,
                         ListAccessService accessService) {
        this.listRepository = listRepository;
        this.memberRepository = memberRepository;
        this.inviteRepository = inviteRepository;
        this.accessService = accessService;
    }

    @Transactional
    public InviteResponse createInvite(Long listId, InviteRequest req, User requester) {
        TaskList list = listRepository.findById(listId)
                .orElseThrow(() -> new EntityNotFoundException("Task list not found: " + listId));
        accessService.requireRole(list, requester, ListRole.OWNER);

        var invite = new ListInvite();
        invite.setTaskList(list);
        invite.setToken(generateToken());
        invite.setRole(req.role());
        invite.setEmail(req.email());
        invite.setCreatedBy(requester);
        invite.setExpiresAt(Instant.now().plus(EXPIRY_DAYS, ChronoUnit.DAYS));

        return InviteResponse.from(inviteRepository.save(invite));
    }

    public InviteResponse previewInvite(String token) {
        ListInvite invite = findValidToken(token);
        return InviteResponse.from(invite);
    }

    @Transactional
    public void acceptInvite(String token, User user) {
        ListInvite invite = findValidToken(token);

        if (invite.isExpired()) {
            throw new IllegalStateException("Invite link has expired");
        }

        TaskList list = invite.getTaskList();

        if (memberRepository.existsByTaskListAndUser(list, user)) {
            return;
        }

        var membership = new TaskListMember();
        membership.setTaskList(list);
        membership.setUser(user);
        membership.setRole(invite.getRole());
        memberRepository.save(membership);

        invite.setAcceptedAt(Instant.now());
        invite.setAcceptedBy(user);
        inviteRepository.save(invite);
    }

    private ListInvite findValidToken(String token) {
        return inviteRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invite not found"));
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
