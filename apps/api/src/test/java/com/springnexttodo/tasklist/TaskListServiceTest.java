package com.springnexttodo.tasklist;

import com.springnexttodo.auth.User;
import com.springnexttodo.common.ForbiddenException;
import com.springnexttodo.tasklist.dto.TaskListRequest;
import com.springnexttodo.tasklist.dto.TaskListResponse;
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
class TaskListServiceTest {

    @Mock private TaskListRepository listRepository;
    @Mock private TaskListMemberRepository memberRepository;
    @Mock private ListAccessService accessService;

    @InjectMocks
    private TaskListService taskListService;

    private User owner;
    private User editor;
    private TaskList list;
    private TaskListMember ownerMember;
    private TaskListMember editorMember;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setEmail("owner@test.com");
        owner.setName("Owner");

        editor = new User();
        editor.setEmail("editor@test.com");
        editor.setName("Editor");

        list = new TaskList();
        list.setName("My List");
        list.setOwner(owner);

        ownerMember = new TaskListMember();
        ownerMember.setTaskList(list);
        ownerMember.setUser(owner);
        ownerMember.setRole(ListRole.OWNER);

        editorMember = new TaskListMember();
        editorMember.setTaskList(list);
        editorMember.setUser(editor);
        editorMember.setRole(ListRole.EDITOR);
    }

    @Test
    void create_saves_list_and_creates_owner_membership() {
        TaskListRequest req = new TaskListRequest("My List");
        when(listRepository.save(any(TaskList.class))).thenAnswer(inv -> inv.getArgument(0));
        when(memberRepository.save(any(TaskListMember.class))).thenAnswer(inv -> inv.getArgument(0));

        taskListService.create(req, owner);

        ArgumentCaptor<TaskList> listCaptor = ArgumentCaptor.forClass(TaskList.class);
        verify(listRepository).save(listCaptor.capture());
        assertThat(listCaptor.getValue().getName()).isEqualTo("My List");
        assertThat(listCaptor.getValue().getOwner()).isSameAs(owner);

        ArgumentCaptor<TaskListMember> memberCaptor = ArgumentCaptor.forClass(TaskListMember.class);
        verify(memberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getRole()).isEqualTo(ListRole.OWNER);
        assertThat(memberCaptor.getValue().getUser()).isSameAs(owner);
    }

    @Test
    void create_response_includes_role() {
        TaskListRequest req = new TaskListRequest("My List");
        when(listRepository.save(any(TaskList.class))).thenAnswer(inv -> inv.getArgument(0));
        when(memberRepository.save(any(TaskListMember.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskListResponse response = taskListService.create(req, owner);

        assertThat(response.name()).isEqualTo("My List");
        assertThat(response.role()).isEqualTo(ListRole.OWNER);
    }

    @Test
    void findAll_returns_lists_with_member_role() {
        when(memberRepository.findByUser(owner)).thenReturn(List.of(ownerMember));

        List<TaskListResponse> result = taskListService.findAll(owner);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("My List");
        assertThat(result.get(0).role()).isEqualTo(ListRole.OWNER);
    }

    @Test
    void findAll_returns_multiple_lists_with_correct_roles() {
        when(memberRepository.findByUser(owner)).thenReturn(List.of(ownerMember, editorMember));

        List<TaskListResponse> result = taskListService.findAll(owner);

        assertThat(result).hasSize(2);
    }

    @Test
    void rename_updates_name_when_requester_is_owner() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(list));
        when(accessService.requireRole(list, owner, ListRole.OWNER)).thenReturn(ownerMember);
        when(listRepository.save(any(TaskList.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskListResponse response = taskListService.rename(1L, new TaskListRequest("Renamed"), owner);

        assertThat(response.name()).isEqualTo("Renamed");
    }

    @Test
    void rename_throws_403_when_editor_tries_to_rename() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(list));
        when(accessService.requireRole(list, editor, ListRole.OWNER))
                .thenThrow(new ForbiddenException("Insufficient role"));

        assertThatThrownBy(() -> taskListService.rename(1L, new TaskListRequest("X"), editor))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void rename_throws_404_for_nonexistent_list() {
        when(listRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskListService.rename(99L, new TaskListRequest("X"), owner))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_removes_list_when_owner() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(list));
        when(accessService.requireRole(list, owner, ListRole.OWNER)).thenReturn(ownerMember);

        taskListService.delete(1L, owner);

        verify(listRepository).delete(list);
    }

    @Test
    void delete_throws_403_when_editor_tries_to_delete() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(list));
        when(accessService.requireRole(list, editor, ListRole.OWNER))
                .thenThrow(new ForbiddenException("Insufficient role"));

        assertThatThrownBy(() -> taskListService.delete(1L, editor))
                .isInstanceOf(ForbiddenException.class);
    }
}
