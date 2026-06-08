package com.springnexttodo.tasklist;

import com.springnexttodo.auth.User;
import com.springnexttodo.auth.UserRepository;
import com.springnexttodo.common.ForbiddenException;
import com.springnexttodo.tasklist.dto.MemberRequest;
import com.springnexttodo.tasklist.dto.MemberResponse;
import com.springnexttodo.tasklist.dto.RoleUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock private TaskListRepository listRepository;
    @Mock private TaskListMemberRepository memberRepository;
    @Mock private UserRepository userRepository;
    @Mock private ListAccessService accessService;

    @InjectMocks
    private MemberService memberService;

    private User owner;
    private User newMember;
    private TaskList list;
    private TaskListMember ownerMembership;
    private TaskListMember editorMembership;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setEmail("owner@test.com");
        owner.setName("Owner");

        newMember = new User();
        newMember.setEmail("member@test.com");
        newMember.setName("Member");

        list = new TaskList();
        list.setName("Test List");
        list.setOwner(owner);

        ownerMembership = new TaskListMember();
        ownerMembership.setTaskList(list);
        ownerMembership.setUser(owner);
        ownerMembership.setRole(ListRole.OWNER);

        editorMembership = new TaskListMember();
        editorMembership.setTaskList(list);
        editorMembership.setUser(newMember);
        editorMembership.setRole(ListRole.EDITOR);
    }

    @Test
    void listMembers_returns_all_members() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(list));
        when(accessService.requireRole(list, owner, ListRole.VIEWER)).thenReturn(ownerMembership);
        when(memberRepository.findByTaskList(list)).thenReturn(List.of(ownerMembership, editorMembership));

        List<MemberResponse> result = memberService.listMembers(1L, owner);

        assertThat(result).hasSize(2);
    }

    @Test
    void addMember_saves_new_membership_when_owner_requests() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(list));
        when(accessService.requireRole(list, owner, ListRole.OWNER)).thenReturn(ownerMembership);
        when(userRepository.findByEmail("member@test.com")).thenReturn(Optional.of(newMember));
        when(memberRepository.existsByTaskListAndUser(list, newMember)).thenReturn(false);
        when(memberRepository.save(any(TaskListMember.class))).thenAnswer(inv -> inv.getArgument(0));

        MemberResponse response = memberService.addMember(1L, new MemberRequest("member@test.com", ListRole.EDITOR), owner);

        assertThat(response.email()).isEqualTo("member@test.com");
        assertThat(response.role()).isEqualTo(ListRole.EDITOR);
    }

    @Test
    void addMember_throws_404_when_email_not_registered() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(list));
        when(accessService.requireRole(list, owner, ListRole.OWNER)).thenReturn(ownerMembership);
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.addMember(1L, new MemberRequest("unknown@test.com", ListRole.VIEWER), owner))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void addMember_throws_when_user_already_member() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(list));
        when(accessService.requireRole(list, owner, ListRole.OWNER)).thenReturn(ownerMembership);
        when(userRepository.findByEmail("member@test.com")).thenReturn(Optional.of(newMember));
        when(memberRepository.existsByTaskListAndUser(list, newMember)).thenReturn(true);

        assertThatThrownBy(() -> memberService.addMember(1L, new MemberRequest("member@test.com", ListRole.EDITOR), owner))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateRole_changes_role_when_owner_requests() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(list));
        when(accessService.requireRole(list, owner, ListRole.OWNER)).thenReturn(ownerMembership);
        when(userRepository.findById(2L)).thenReturn(Optional.of(newMember));
        when(memberRepository.findByTaskListAndUser(list, newMember)).thenReturn(Optional.of(editorMembership));
        when(memberRepository.save(any(TaskListMember.class))).thenAnswer(inv -> inv.getArgument(0));

        MemberResponse response = memberService.updateRole(1L, 2L, new RoleUpdateRequest(ListRole.VIEWER), owner);

        assertThat(response.role()).isEqualTo(ListRole.VIEWER);
    }

    @Test
    void updateRole_throws_when_trying_to_change_owner_role() {
        User ownerUser = owner;
        when(listRepository.findById(1L)).thenReturn(Optional.of(list));
        when(accessService.requireRole(list, owner, ListRole.OWNER)).thenReturn(ownerMembership);
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(memberRepository.findByTaskListAndUser(list, ownerUser)).thenReturn(Optional.of(ownerMembership));

        assertThatThrownBy(() -> memberService.updateRole(1L, 1L, new RoleUpdateRequest(ListRole.VIEWER), owner))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void removeMember_allows_owner_to_remove_another_member() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(list));
        when(accessService.requireRole(list, owner, ListRole.OWNER)).thenReturn(ownerMembership);
        when(userRepository.findById(2L)).thenReturn(Optional.of(newMember));
        when(memberRepository.findByTaskListAndUser(list, newMember)).thenReturn(Optional.of(editorMembership));

        memberService.removeMember(1L, 2L, owner);

        verify(memberRepository).delete(editorMembership);
    }

    @Test
    void removeMember_allows_member_to_leave_own_list() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(list));
        when(accessService.requireMembership(list, newMember)).thenReturn(editorMembership);
        when(userRepository.findById(2L)).thenReturn(Optional.of(newMember));
        when(memberRepository.findByTaskListAndUser(list, newMember)).thenReturn(Optional.of(editorMembership));

        memberService.removeMember(1L, 2L, newMember);

        verify(memberRepository).delete(editorMembership);
    }

    @Test
    void removeMember_throws_when_non_owner_removes_other_member() {
        User thirdUser = new User();
        thirdUser.setEmail("third@test.com");

        when(listRepository.findById(1L)).thenReturn(Optional.of(list));
        when(userRepository.findById(99L)).thenReturn(Optional.of(thirdUser));
        when(accessService.requireRole(list, newMember, ListRole.OWNER))
                .thenThrow(new ForbiddenException("Insufficient role"));

        assertThatThrownBy(() -> memberService.removeMember(1L, 99L, newMember))
                .isInstanceOf(ForbiddenException.class);
    }
}
