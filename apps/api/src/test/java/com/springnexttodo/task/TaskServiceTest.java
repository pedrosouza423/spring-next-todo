package com.springnexttodo.task;

import com.springnexttodo.auth.User;
import com.springnexttodo.category.Category;
import com.springnexttodo.category.CategoryService;
import com.springnexttodo.task.dto.TaskRequest;
import com.springnexttodo.task.dto.TaskResponse;
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

    @Mock
    private TaskRepository repository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Pedro");
        user.setEmail("pedro@example.com");

        task = new Task();
        task.setTitle("Test task");
        task.setDescription("A description");
        task.setUser(user);
    }

    @Test
    void findAll_returns_only_user_tasks() {
        Task another = new Task();
        another.setTitle("Another task");
        another.setUser(user);
        when(repository.findFiltered(user, null, null, null)).thenReturn(List.of(task, another));

        List<TaskResponse> result = taskService.findAll(user, null, null, null);

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

        when(repository.findFiltered(user, 1L, null, null)).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.findAll(user, 1L, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Test task");
        verify(repository).findFiltered(user, 1L, null, null);
    }

    @Test
    void findAll_filtered_by_priority() {
        task.setPriority(Priority.HIGH);
        when(repository.findFiltered(user, null, Priority.HIGH, null)).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.findAll(user, null, Priority.HIGH, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).priority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void findAll_filtered_by_completed() {
        task.setCompleted(false);
        when(repository.findFiltered(user, null, null, false)).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.findAll(user, null, null, false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).completed()).isFalse();
    }

    @Test
    void findById_returns_task_belonging_to_user() {
        when(repository.findByIdAndUser(1L, user)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.findById(1L, user);

        assertThat(response.title()).isEqualTo("Test task");
    }

    @Test
    void findById_task_belongs_to_other_user() {
        when(repository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(1L, user))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_sets_user_on_task() {
        TaskRequest req = new TaskRequest("New task", "desc", null, null, null);
        when(repository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        taskService.create(req, user);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getTitle()).isEqualTo("New task");
        assertThat(captor.getValue().getCategory()).isNull();
    }

    @Test
    void create_without_priority_defaults_to_medium() {
        TaskRequest req = new TaskRequest("New task", null, null, null, null);
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        taskService.create(req, user);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPriority()).isEqualTo(Priority.MEDIUM);
    }

    @Test
    void create_with_explicit_priority_persists_it() {
        TaskRequest req = new TaskRequest("New task", null, null, null, Priority.HIGH);
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

        TaskRequest req = new TaskRequest("New task", null, 1L, null, null);
        when(categoryService.getEntityById(1L, user)).thenReturn(category);
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        taskService.create(req, user);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCategory()).isSameAs(category);
    }

    @Test
    void create_with_invalid_category_throws() {
        when(categoryService.getEntityById(99L, user))
                .thenThrow(new EntityNotFoundException("Category not found: 99"));

        assertThatThrownBy(() -> taskService.create(new TaskRequest("New task", null, 99L, null, null), user))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_with_due_date_persists_it() {
        LocalDate due = LocalDate.of(2026, 12, 31);
        TaskRequest req = new TaskRequest("Task with due", null, null, due, null);
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        taskService.create(req, user);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDueDate()).isEqualTo(due);
    }

    @Test
    void create_with_null_due_date_leaves_it_null() {
        TaskRequest req = new TaskRequest("No due date", null, null, null, null);
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        taskService.create(req, user);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDueDate()).isNull();
    }

    @Test
    void delete_task_of_other_user() {
        when(repository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(99L, user))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
