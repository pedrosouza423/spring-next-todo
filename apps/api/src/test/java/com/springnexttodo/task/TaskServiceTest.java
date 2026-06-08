package com.springnexttodo.task;

import com.springnexttodo.auth.User;
import com.springnexttodo.category.Category;
import com.springnexttodo.category.CategoryService;
import com.springnexttodo.common.ForbiddenException;
import com.springnexttodo.task.dto.TaskRequest;
import com.springnexttodo.task.dto.TaskResponse;
import com.springnexttodo.tasklist.ListAccessService;
import com.springnexttodo.tasklist.ListRole;
import com.springnexttodo.tasklist.TaskList;
import com.springnexttodo.tasklist.TaskListMember;
import com.springnexttodo.tasklist.TaskListRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository repository;
    @Mock private CategoryService categoryService;
    @Mock private TaskListRepository listRepository;
    @Mock private ListAccessService accessService;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;
    private TaskList defaultList;
    private TaskListMember editorMembership;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Pedro");
        user.setEmail("pedro@example.com");

        defaultList = new TaskList();
        defaultList.setName("Minhas Tarefas");
        defaultList.setOwner(user);

        editorMembership = new TaskListMember();
        editorMembership.setTaskList(defaultList);
        editorMembership.setUser(user);
        editorMembership.setRole(ListRole.EDITOR);

        task = new Task();
        task.setTitle("Test task");
        task.setDescription("A description");
        task.setUser(user);
        task.setTaskList(defaultList);
    }

    @Test
    void findAll_returns_only_user_tasks() {
        Task another = new Task();
        another.setTitle("Another task");
        another.setUser(user);
        another.setTaskList(defaultList);
        when(repository.findFiltered(user, null, null, null, null, null)).thenReturn(List.of(task, another));

        List<TaskResponse> result = taskService.findAll(user, null, null, null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Test task");
        assertThat(result.get(1).title()).isEqualTo("Another task");
    }

    @Test
    void findAll_filtered_by_category() {
        Category category = new Category();
        category.setName("Trabalho");
        category.setColor("#3b82f6");
        category.setUser(user);
        task.setCategory(category);

        when(repository.findFiltered(user, null, 1L, null, null, null)).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.findAll(user, null, 1L, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Test task");
        verify(repository).findFiltered(user, null, 1L, null, null, null);
    }

    @Test
    void findAll_filtered_by_priority() {
        task.setPriority(Priority.HIGH);
        when(repository.findFiltered(user, null, null, Priority.HIGH, null, null)).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.findAll(user, null, null, Priority.HIGH, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).priority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void findAll_filtered_by_completed() {
        task.setCompleted(false);
        when(repository.findFiltered(user, null, null, null, false, null)).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.findAll(user, null, null, null, false, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).completed()).isFalse();
    }

    @Test
    void findAll_filtered_by_query_passes_trimmed_value() {
        when(repository.findFiltered(user, null, null, null, null, "comprar")).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.findAll(user, null, null, null, null, "  comprar  ");

        assertThat(result).hasSize(1);
        verify(repository).findFiltered(user, null, null, null, null, "comprar");
    }

    @Test
    void findAll_blank_query_treated_as_null() {
        when(repository.findFiltered(user, null, null, null, null, null)).thenReturn(List.of(task));

        taskService.findAll(user, null, null, null, null, "   ");

        verify(repository).findFiltered(user, null, null, null, null, null);
    }

    @Test
    void findAll_query_escapes_like_wildcards() {
        when(repository.findFiltered(user, null, null, null, null, "100!%")).thenReturn(List.of(task));

        taskService.findAll(user, null, null, null, null, "100%");

        verify(repository).findFiltered(user, null, null, null, null, "100!%");
    }

    @Test
    void findAll_query_escapes_underscore_wildcard() {
        when(repository.findFiltered(user, null, null, null, null, "task!_1")).thenReturn(List.of(task));

        taskService.findAll(user, null, null, null, null, "task_1");

        verify(repository).findFiltered(user, null, null, null, null, "task!_1");
    }

    @Test
    void findAll_query_escapes_exclamation_mark() {
        when(repository.findFiltered(user, null, null, null, null, "hello!!world")).thenReturn(List.of(task));

        taskService.findAll(user, null, null, null, null, "hello!world");

        verify(repository).findFiltered(user, null, null, null, null, "hello!!world");
    }

    @Test
    void findById_returns_task_belonging_to_user() {
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(accessService.requireRole(defaultList, user, ListRole.VIEWER)).thenReturn(editorMembership);

        TaskResponse response = taskService.findById(1L, user);

        assertThat(response.title()).isEqualTo("Test task");
    }

    @Test
    void findById_task_not_found() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(1L, user))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findById_user_not_member_gets_404() {
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(accessService.requireRole(defaultList, user, ListRole.VIEWER))
                .thenThrow(new EntityNotFoundException("Task list not found"));

        assertThatThrownBy(() -> taskService.findById(1L, user))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_sets_user_on_task() {
        TaskRequest req = new TaskRequest("New task", "desc", null, null, null, null);
        when(listRepository.findByOwnerAndIsDefault(user, true)).thenReturn(Optional.of(defaultList));
        when(accessService.requireRole(defaultList, user, ListRole.EDITOR)).thenReturn(editorMembership);
        when(repository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        taskService.create(req, user);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getTitle()).isEqualTo("New task");
        assertThat(captor.getValue().getCategory()).isNull();
    }

    @Test
    void create_sets_task_list_on_task() {
        TaskRequest req = new TaskRequest("New task", null, null, null, null, null);
        when(listRepository.findByOwnerAndIsDefault(user, true)).thenReturn(Optional.of(defaultList));
        when(accessService.requireRole(defaultList, user, ListRole.EDITOR)).thenReturn(editorMembership);
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        taskService.create(req, user);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTaskList()).isSameAs(defaultList);
    }

    @Test
    void create_without_priority_defaults_to_medium() {
        TaskRequest req = new TaskRequest("New task", null, null, null, null, null);
        when(listRepository.findByOwnerAndIsDefault(user, true)).thenReturn(Optional.of(defaultList));
        when(accessService.requireRole(defaultList, user, ListRole.EDITOR)).thenReturn(editorMembership);
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        taskService.create(req, user);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPriority()).isEqualTo(Priority.MEDIUM);
    }

    @Test
    void create_with_explicit_priority_persists_it() {
        TaskRequest req = new TaskRequest("New task", null, null, null, Priority.HIGH, null);
        when(listRepository.findByOwnerAndIsDefault(user, true)).thenReturn(Optional.of(defaultList));
        when(accessService.requireRole(defaultList, user, ListRole.EDITOR)).thenReturn(editorMembership);
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        taskService.create(req, user);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void create_with_valid_category_sets_it() {
        Category category = new Category();
        category.setName("Trabalho");
        category.setColor("#3b82f6");
        category.setUser(user);

        TaskRequest req = new TaskRequest("New task", null, 1L, null, null, null);
        when(listRepository.findByOwnerAndIsDefault(user, true)).thenReturn(Optional.of(defaultList));
        when(accessService.requireRole(defaultList, user, ListRole.EDITOR)).thenReturn(editorMembership);
        when(categoryService.getEntityById(1L, user)).thenReturn(category);
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        taskService.create(req, user);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCategory()).isSameAs(category);
    }

    @Test
    void create_with_invalid_category_throws() {
        when(listRepository.findByOwnerAndIsDefault(user, true)).thenReturn(Optional.of(defaultList));
        when(accessService.requireRole(defaultList, user, ListRole.EDITOR)).thenReturn(editorMembership);
        when(categoryService.getEntityById(99L, user))
                .thenThrow(new EntityNotFoundException("Category not found: 99"));

        assertThatThrownBy(() -> taskService.create(new TaskRequest("New task", null, 99L, null, null, null), user))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_with_due_date_persists_it() {
        LocalDate due = LocalDate.of(2026, 12, 31);
        TaskRequest req = new TaskRequest("Task with due", null, null, due, null, null);
        when(listRepository.findByOwnerAndIsDefault(user, true)).thenReturn(Optional.of(defaultList));
        when(accessService.requireRole(defaultList, user, ListRole.EDITOR)).thenReturn(editorMembership);
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        taskService.create(req, user);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDueDate()).isEqualTo(due);
    }

    @Test
    void create_with_null_due_date_leaves_it_null() {
        TaskRequest req = new TaskRequest("No due date", null, null, null, null, null);
        when(listRepository.findByOwnerAndIsDefault(user, true)).thenReturn(Optional.of(defaultList));
        when(accessService.requireRole(defaultList, user, ListRole.EDITOR)).thenReturn(editorMembership);
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        taskService.create(req, user);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDueDate()).isNull();
    }

    @Test
    void update_without_priority_preserves_existing_priority() {
        task.setPriority(Priority.HIGH);
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskRequest req = new TaskRequest("Updated title", null, null, null, null, null);
        TaskResponse response = taskService.update(1L, req, user);

        assertThat(response.priority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void create_requires_editor_role_on_list() {
        TaskRequest req = new TaskRequest("Task", null, null, null, null, null);
        when(listRepository.findByOwnerAndIsDefault(user, true)).thenReturn(Optional.of(defaultList));
        when(accessService.requireRole(defaultList, user, ListRole.EDITOR))
                .thenThrow(new ForbiddenException("Insufficient role"));

        assertThatThrownBy(() -> taskService.create(req, user))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void delete_task_not_accessible_to_user_throws_404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(99L, user))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_requires_editor_role_on_list() {
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(accessService.requireRole(defaultList, user, ListRole.EDITOR))
                .thenThrow(new ForbiddenException("Insufficient role"));

        assertThatThrownBy(() -> taskService.delete(1L, user))
                .isInstanceOf(ForbiddenException.class);
    }
}
