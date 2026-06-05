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
        when(repository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(task, another));

        List<TaskResponse> result = taskService.findAll(user, null);

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

        when(categoryService.getEntityById(1L, user)).thenReturn(category);
        when(repository.findByUserAndCategoryOrderByCreatedAtDesc(user, category)).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.findAll(user, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Test task");
        verify(repository).findByUserAndCategoryOrderByCreatedAtDesc(user, category);
    }

    @Test
    void findAll_with_unknown_category_throws() {
        when(categoryService.getEntityById(99L, user))
                .thenThrow(new EntityNotFoundException("Category not found: 99"));

        assertThatThrownBy(() -> taskService.findAll(user, 99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
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
        TaskRequest req = new TaskRequest("New task", "desc", null);
        when(repository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        taskService.create(req, user);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getTitle()).isEqualTo("New task");
        assertThat(captor.getValue().getCategory()).isNull();
    }

    @Test
    void create_with_valid_category_sets_it() {
        Category category = new Category();
        category.setName("Trabalho");
        category.setColor("#3b82f6");
        category.setUser(user);

        TaskRequest req = new TaskRequest("New task", null, 1L);
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

        assertThatThrownBy(() -> taskService.create(new TaskRequest("New task", null, 99L), user))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_task_of_other_user() {
        when(repository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(99L, user))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
