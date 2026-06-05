package com.springnexttodo.category;

import com.springnexttodo.auth.User;
import com.springnexttodo.category.dto.CategoryRequest;
import com.springnexttodo.category.dto.CategoryResponse;
import com.springnexttodo.task.TaskRepository;
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
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Pedro");
        user.setEmail("pedro@example.com");

        category = new Category();
        category.setName("Trabalho");
        category.setColor("#3b82f6");
        category.setUser(user);
    }

    @Test
    void findAll_returns_only_user_categories() {
        Category another = new Category();
        another.setName("Estudo");
        another.setColor("#10b981");
        another.setUser(user);
        when(categoryRepository.findByUserOrderByName(user)).thenReturn(List.of(category, another));

        List<CategoryResponse> result = categoryService.findAll(user);

        assertThat(result).extracting(CategoryResponse::name)
                .containsExactly("Trabalho", "Estudo");
    }

    @Test
    void getEntityById_returns_category_belonging_to_user() {
        when(categoryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(category));
        Category result = categoryService.getEntityById(1L, user);
        assertThat(result).isSameAs(category);
    }

    @Test
    void getEntityById_throws_when_category_not_found() {
        when(categoryRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.getEntityById(99L, user))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_saves_category_with_user() {
        CategoryRequest req = new CategoryRequest("Trabalho", "#3b82f6");
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        categoryService.create(req, user);

        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Trabalho");
        assertThat(captor.getValue().getColor()).isEqualTo("#3b82f6");
        assertThat(captor.getValue().getUser()).isSameAs(user);
    }

    @Test
    void update_changes_name_and_color() {
        when(categoryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryResponse result = categoryService.update(1L, new CategoryRequest("Pessoal", "#f59e0b"), user);

        assertThat(result.name()).isEqualTo("Pessoal");
        assertThat(result.color()).isEqualTo("#f59e0b");
    }

    @Test
    void update_category_of_other_user_throws() {
        when(categoryRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(99L, new CategoryRequest("X", "#000000"), user))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_clears_tasks_and_removes_category() {
        when(categoryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(category));

        categoryService.delete(1L, user);

        verify(taskRepository).clearCategory(category);
        verify(categoryRepository).delete(category);
    }

    @Test
    void delete_category_of_other_user_throws() {
        when(categoryRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(99L, user))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
