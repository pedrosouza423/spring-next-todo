package com.springnexttodo.tasklist;

import com.springnexttodo.auth.User;
import com.springnexttodo.tasklist.dto.InviteRequest;
import com.springnexttodo.tasklist.dto.InviteResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InviteServiceTest {

    @Mock private TaskListRepository listRepository;
    @Mock private TaskListMemberRepository memberRepository;
    @Mock private ListInviteRepository inviteRepository;
    @Mock private ListAccessService accessService;

    @InjectMocks
    private InviteService inviteService;

    private User owner;
    private User invitee;
    private TaskList list;
    private TaskListMember ownerMembership;
    private ListInvite validInvite;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setEmail("owner@test.com");
        owner.setName("Owner");

        invitee = new User();
        invitee.setEmail("invitee@test.com");
        invitee.setName("Invitee");

        list = new TaskList();
        list.setName("Shared List");
        list.setOwner(owner);

        ownerMembership = new TaskListMember();
        ownerMembership.setTaskList(list);
        ownerMembership.setUser(owner);
        ownerMembership.setRole(ListRole.OWNER);

        validInvite = new ListInvite();
        validInvite.setTaskList(list);
        validInvite.setToken("valid-token-abc");
        validInvite.setRole(ListRole.EDITOR);
        validInvite.setCreatedBy(owner);
        validInvite.setExpiresAt(Instant.now().plusSeconds(86400));
    }

    @Test
    void createInvite_generates_token_and_saves() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(list));
        when(accessService.requireRole(list, owner, ListRole.OWNER)).thenReturn(ownerMembership);
        when(inviteRepository.save(any(ListInvite.class))).thenAnswer(inv -> inv.getArgument(0));

        InviteResponse response = inviteService.createInvite(1L, new InviteRequest(ListRole.EDITOR, null), owner);

        assertThat(response.token()).isNotBlank();
        assertThat(response.role()).isEqualTo(ListRole.EDITOR);
        assertThat(response.listName()).isEqualTo("Shared List");
        assertThat(response.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void createInvite_token_is_unique_each_call() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(list));
        when(accessService.requireRole(list, owner, ListRole.OWNER)).thenReturn(ownerMembership);
        when(inviteRepository.save(any(ListInvite.class))).thenAnswer(inv -> inv.getArgument(0));

        InviteResponse r1 = inviteService.createInvite(1L, new InviteRequest(ListRole.VIEWER, null), owner);
        InviteResponse r2 = inviteService.createInvite(1L, new InviteRequest(ListRole.VIEWER, null), owner);

        assertThat(r1.token()).isNotEqualTo(r2.token());
    }

    @Test
    void previewInvite_returns_invite_info_for_valid_token() {
        when(inviteRepository.findByToken("valid-token-abc")).thenReturn(Optional.of(validInvite));

        InviteResponse response = inviteService.previewInvite("valid-token-abc");

        assertThat(response.role()).isEqualTo(ListRole.EDITOR);
        assertThat(response.listName()).isEqualTo("Shared List");
    }

    @Test
    void previewInvite_throws_404_for_unknown_token() {
        when(inviteRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inviteService.previewInvite("bad-token"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void acceptInvite_creates_membership_with_invite_role() {
        when(inviteRepository.findByToken("valid-token-abc")).thenReturn(Optional.of(validInvite));
        when(memberRepository.existsByTaskListAndUser(list, invitee)).thenReturn(false);
        when(memberRepository.save(any(TaskListMember.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inviteRepository.save(any(ListInvite.class))).thenAnswer(inv -> inv.getArgument(0));

        inviteService.acceptInvite("valid-token-abc", invitee);

        ArgumentCaptor<TaskListMember> captor = ArgumentCaptor.forClass(TaskListMember.class);
        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(ListRole.EDITOR);
        assertThat(captor.getValue().getUser()).isSameAs(invitee);
    }

    @Test
    void acceptInvite_marks_invite_as_accepted() {
        when(inviteRepository.findByToken("valid-token-abc")).thenReturn(Optional.of(validInvite));
        when(memberRepository.existsByTaskListAndUser(list, invitee)).thenReturn(false);
        when(memberRepository.save(any(TaskListMember.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inviteRepository.save(any(ListInvite.class))).thenAnswer(inv -> inv.getArgument(0));

        inviteService.acceptInvite("valid-token-abc", invitee);

        ArgumentCaptor<ListInvite> captor = ArgumentCaptor.forClass(ListInvite.class);
        verify(inviteRepository).save(captor.capture());
        assertThat(captor.getValue().getAcceptedAt()).isNotNull();
        assertThat(captor.getValue().getAcceptedBy()).isSameAs(invitee);
    }

    @Test
    void acceptInvite_throws_when_expired() {
        validInvite.setExpiresAt(Instant.now().minusSeconds(1));
        when(inviteRepository.findByToken("valid-token-abc")).thenReturn(Optional.of(validInvite));

        assertThatThrownBy(() -> inviteService.acceptInvite("valid-token-abc", invitee))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void acceptInvite_is_idempotent_when_already_member() {
        when(inviteRepository.findByToken("valid-token-abc")).thenReturn(Optional.of(validInvite));
        when(memberRepository.existsByTaskListAndUser(list, invitee)).thenReturn(true);

        inviteService.acceptInvite("valid-token-abc", invitee);

        verify(memberRepository, never()).save(any());
    }
}
