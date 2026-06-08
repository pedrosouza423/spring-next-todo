package com.springnexttodo.tasklist;

import com.springnexttodo.auth.User;
import com.springnexttodo.common.ForbiddenException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAccessServiceTest {

    @Mock
    private TaskListMemberRepository memberRepository;

    @InjectMocks
    private ListAccessService accessService;

    private User owner;
    private User editor;
    private User viewer;
    private User stranger;
    private TaskList list;
    private TaskListMember ownerMember;
    private TaskListMember editorMember;
    private TaskListMember viewerMember;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setEmail("owner@test.com");

        editor = new User();
        editor.setEmail("editor@test.com");

        viewer = new User();
        viewer.setEmail("viewer@test.com");

        stranger = new User();
        stranger.setEmail("stranger@test.com");

        list = new TaskList();
        list.setName("Test List");

        ownerMember = new TaskListMember();
        ownerMember.setTaskList(list);
        ownerMember.setUser(owner);
        ownerMember.setRole(ListRole.OWNER);

        editorMember = new TaskListMember();
        editorMember.setTaskList(list);
        editorMember.setUser(editor);
        editorMember.setRole(ListRole.EDITOR);

        viewerMember = new TaskListMember();
        viewerMember.setTaskList(list);
        viewerMember.setUser(viewer);
        viewerMember.setRole(ListRole.VIEWER);
    }

    @Test
    void requireMembership_returns_member_when_user_is_member() {
        when(memberRepository.findByTaskListAndUser(list, owner)).thenReturn(Optional.of(ownerMember));

        TaskListMember result = accessService.requireMembership(list, owner);

        assertThat(result).isSameAs(ownerMember);
    }

    @Test
    void requireMembership_throws_404_when_user_is_not_member() {
        when(memberRepository.findByTaskListAndUser(list, stranger)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accessService.requireMembership(list, stranger))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void requireRole_owner_passes_owner_check() {
        when(memberRepository.findByTaskListAndUser(list, owner)).thenReturn(Optional.of(ownerMember));

        TaskListMember result = accessService.requireRole(list, owner, ListRole.OWNER);

        assertThat(result.getRole()).isEqualTo(ListRole.OWNER);
    }

    @Test
    void requireRole_owner_passes_editor_check() {
        when(memberRepository.findByTaskListAndUser(list, owner)).thenReturn(Optional.of(ownerMember));

        TaskListMember result = accessService.requireRole(list, owner, ListRole.EDITOR);

        assertThat(result.getRole()).isEqualTo(ListRole.OWNER);
    }

    @Test
    void requireRole_owner_passes_viewer_check() {
        when(memberRepository.findByTaskListAndUser(list, owner)).thenReturn(Optional.of(ownerMember));

        TaskListMember result = accessService.requireRole(list, owner, ListRole.VIEWER);

        assertThat(result.getRole()).isEqualTo(ListRole.OWNER);
    }

    @Test
    void requireRole_editor_passes_editor_check() {
        when(memberRepository.findByTaskListAndUser(list, editor)).thenReturn(Optional.of(editorMember));

        TaskListMember result = accessService.requireRole(list, editor, ListRole.EDITOR);

        assertThat(result.getRole()).isEqualTo(ListRole.EDITOR);
    }

    @Test
    void requireRole_editor_fails_owner_check() {
        when(memberRepository.findByTaskListAndUser(list, editor)).thenReturn(Optional.of(editorMember));

        assertThatThrownBy(() -> accessService.requireRole(list, editor, ListRole.OWNER))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void requireRole_viewer_fails_editor_check() {
        when(memberRepository.findByTaskListAndUser(list, viewer)).thenReturn(Optional.of(viewerMember));

        assertThatThrownBy(() -> accessService.requireRole(list, viewer, ListRole.EDITOR))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void requireRole_viewer_fails_owner_check() {
        when(memberRepository.findByTaskListAndUser(list, viewer)).thenReturn(Optional.of(viewerMember));

        assertThatThrownBy(() -> accessService.requireRole(list, viewer, ListRole.OWNER))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void requireRole_stranger_throws_404_not_403() {
        when(memberRepository.findByTaskListAndUser(list, stranger)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accessService.requireRole(list, stranger, ListRole.VIEWER))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
