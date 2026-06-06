# Categories Feature Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add user-scoped categories to tasks — CRUD API, optional category on task create/update/filter, and frontend badge + filter UI.

**Architecture:** New `com.springnexttodo.category` package mirrors the `task` pattern (Entity → Repository → Service → Controller, DTOs as Records). `Task` gains a nullable `@ManyToOne Category` FK. Deleting a category first bulk-nullifies all referring tasks (same transaction). Frontend prop-drills categories from `page.tsx` → `TaskList` → `TaskForm` / `TaskItem` → `TaskEditDialog`.

**Tech Stack:** Spring Boot 3.5, Java 17, JPA/H2, JUnit 5 + Mockito, Next.js 15, TypeScript, Tailwind, shadcn/ui

---

## File Map

**Create (backend):**
- `apps/api/src/main/java/com/springnexttodo/category/Category.java`
- `apps/api/src/main/java/com/springnexttodo/category/CategoryRepository.java`
- `apps/api/src/main/java/com/springnexttodo/category/CategoryService.java`
- `apps/api/src/main/java/com/springnexttodo/category/CategoryController.java`
- `apps/api/src/main/java/com/springnexttodo/category/dto/CategoryRequest.java`
- `apps/api/src/main/java/com/springnexttodo/category/dto/CategoryResponse.java`
- `apps/api/src/test/java/com/springnexttodo/category/CategoryServiceTest.java`

**Modify (backend):**
- `apps/api/src/main/java/com/springnexttodo/task/Task.java` (category field)
- `apps/api/src/main/java/com/springnexttodo/task/TaskRepository.java` (clearCategory + findByUserAndCategory — done in Task 2 Step 5)
- `apps/api/src/main/java/com/springnexttodo/task/dto/TaskRequest.java`
- `apps/api/src/main/java/com/springnexttodo/task/dto/TaskResponse.java`
- `apps/api/src/main/java/com/springnexttodo/task/TaskService.java`
- `apps/api/src/main/java/com/springnexttodo/task/TaskController.java`
- `apps/api/src/main/java/com/springnexttodo/config/SeedData.java`
- `apps/api/src/test/java/com/springnexttodo/task/TaskServiceTest.java`

**Create (frontend):**
- `apps/web/src/components/tasks/CategoryBadge.tsx`
- `apps/web/src/components/tasks/CategorySelector.tsx`

**Modify (frontend):**
- `apps/web/src/lib/api.ts`
- `apps/web/src/components/tasks/TaskItem.tsx`
- `apps/web/src/components/tasks/TaskForm.tsx`
- `apps/web/src/components/tasks/TaskEditDialog.tsx`
- `apps/web/src/components/tasks/TaskList.tsx`
- `apps/web/src/app/page.tsx`

---

## Task 1: Create branch

**Files:** none

- [ ] **Step 1: Create and switch to feature branch**

Run from `apps/api` or repo root:
```bash
git checkout -b feat/categories
```

Expected: `Switched to a new branch 'feat/categories'`

---

## Task 2: Category entity, DTOs, and Repository

**Files:**
- Create: `apps/api/src/main/java/com/springnexttodo/category/Category.java`
- Create: `apps/api/src/main/java/com/springnexttodo/category/dto/CategoryRequest.java`
- Create: `apps/api/src/main/java/com/springnexttodo/category/dto/CategoryResponse.java`
- Create: `apps/api/src/main/java/com/springnexttodo/category/CategoryRepository.java`

- [ ] **Step 1: Create Category entity**

`apps/api/src/main/java/com/springnexttodo/category/Category.java`:
```java
package com.springnexttodo.category;

import com.springnexttodo.auth.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false, length = 7)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
```

- [ ] **Step 2: Create CategoryRequest DTO**

`apps/api/src/main/java/com/springnexttodo/category/dto/CategoryRequest.java`:
```java
package com.springnexttodo.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
    @NotBlank(message = "name is required")
    @Size(max = 50, message = "name must be at most 50 characters")
    String name,

    @NotBlank(message = "color is required")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "color must be a valid hex color (e.g. #3b82f6)")
    String color
) {}
```

- [ ] **Step 3: Create CategoryResponse DTO**

`apps/api/src/main/java/com/springnexttodo/category/dto/CategoryResponse.java`:
```java
package com.springnexttodo.category.dto;

import com.springnexttodo.category.Category;

public record CategoryResponse(Long id, String name, String color) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getColor());
    }
}
```

- [ ] **Step 4: Create CategoryRepository**

`apps/api/src/main/java/com/springnexttodo/category/CategoryRepository.java`:
```java
package com.springnexttodo.category;

import com.springnexttodo.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserOrderByName(User user);
    Optional<Category> findByIdAndUser(Long id, User user);
}
```

- [ ] **Step 5: Add category-related methods to TaskRepository now**

`CategoryService.delete` calls `taskRepository.clearCategory(category)` and `CategoryServiceTest` verifies it — both require the method to exist at compile time. Add it here so Tasks 3 and 4 compile cleanly.

Replace `apps/api/src/main/java/com/springnexttodo/task/TaskRepository.java`:
```java
package com.springnexttodo.task;

import com.springnexttodo.auth.User;
import com.springnexttodo.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserOrderByCreatedAtDesc(User user);
    Optional<Task> findByIdAndUser(Long id, User user);
    List<Task> findByUserAndCategoryOrderByCreatedAtDesc(User user, Category category);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Task t SET t.category = null WHERE t.category = :category")
    void clearCategory(@Param("category") Category category);
}
```

`clearAutomatically = true` flushes the persistence context after the bulk UPDATE so subsequent reads in the same transaction see the nullified state.

---

## Task 3: CategoryService — write failing tests then implement

**Files:**
- Create: `apps/api/src/test/java/com/springnexttodo/category/CategoryServiceTest.java`
- Create: `apps/api/src/main/java/com/springnexttodo/category/CategoryService.java`

- [ ] **Step 1: Write CategoryServiceTest**

`apps/api/src/test/java/com/springnexttodo/category/CategoryServiceTest.java`:
```java
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

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Trabalho");
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
```

- [ ] **Step 2: Run test — expect compile failure (CategoryService doesn't exist)**

```bash
cd apps/api && ./mvnw test -Dtest=CategoryServiceTest
```

Expected: compilation error — `CategoryService` not found. This is the TDD red phase. `TaskRepository.clearCategory` already exists (added in Task 2 Step 5) so that part compiles fine.

- [ ] **Step 3: Implement CategoryService**

`apps/api/src/main/java/com/springnexttodo/category/CategoryService.java`:
```java
package com.springnexttodo.category;

import com.springnexttodo.auth.User;
import com.springnexttodo.category.dto.CategoryRequest;
import com.springnexttodo.category.dto.CategoryResponse;
import com.springnexttodo.task.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository repository;
    private final TaskRepository taskRepository;

    public CategoryService(CategoryRepository repository, TaskRepository taskRepository) {
        this.repository = repository;
        this.taskRepository = taskRepository;
    }

    public List<CategoryResponse> findAll(User user) {
        return repository.findByUserOrderByName(user)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public Category getEntityById(Long id, User user) {
        return repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req, User user) {
        var category = new Category();
        category.setName(req.name());
        category.setColor(req.color());
        category.setUser(user);
        return CategoryResponse.from(repository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest req, User user) {
        var category = getEntityById(id, user);
        category.setName(req.name());
        category.setColor(req.color());
        return CategoryResponse.from(repository.save(category));
    }

    @Transactional
    public void delete(Long id, User user) {
        var category = getEntityById(id, user);
        taskRepository.clearCategory(category);
        repository.delete(category);
    }
}
```

Note: `taskRepository.clearCategory(category)` calls a `@Modifying @Query` that doesn't exist yet in `TaskRepository` — it will be added in Task 5. For now `CategoryService` compiles; the test will fail at runtime with "No property 'clearCategory' found". That's acceptable — the backend tasks are sequential and `TaskRepository` is extended in Task 5.

- [ ] **Step 4: Run tests — will now compile but clearCategory fails**

```bash
cd apps/api && ./mvnw test -Dtest=CategoryServiceTest
```

Expected: 5 tests pass, 1 fails (`delete_clears_tasks_and_removes_category`) with method-not-found on `clearCategory`. Proceed to Task 4.

---

## Task 4: CategoryController

**Files:**
- Create: `apps/api/src/main/java/com/springnexttodo/category/CategoryController.java`

- [ ] **Step 1: Create CategoryController**

`apps/api/src/main/java/com/springnexttodo/category/CategoryController.java`:
```java
package com.springnexttodo.category;

import com.springnexttodo.auth.AuthService;
import com.springnexttodo.auth.User;
import com.springnexttodo.category.dto.CategoryRequest;
import com.springnexttodo.category.dto.CategoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService service;
    private final AuthService authService;

    public CategoryController(CategoryService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    private User currentUser(Authentication auth) {
        return authService.getUser(auth.getName());
    }

    @GetMapping
    public List<CategoryResponse> list(Authentication auth) {
        return service.findAll(currentUser(auth));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest req, Authentication auth) {
        return service.create(req, currentUser(auth));
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req, Authentication auth) {
        return service.update(id, req, currentUser(auth));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, currentUser(auth));
    }
}
```

- [ ] **Step 2: Commit Category CRUD scaffold**

```bash
git add apps/api/src/main/java/com/springnexttodo/category \
        apps/api/src/test/java/com/springnexttodo/category
git commit -m "feat(api): add Category entity, DTOs, repository, service and controller"
```

---

## Task 5: Add category field to Task entity

**Files:**
- Modify: `apps/api/src/main/java/com/springnexttodo/task/Task.java`

Note: `TaskRepository` was already updated in Task 2 Step 5 to avoid compilation issues in CategoryService/CategoryServiceTest.

- [ ] **Step 1: Add category field to Task entity**

In `apps/api/src/main/java/com/springnexttodo/task/Task.java`, add the import and field after the `user` field:

Add import (with the other imports):
```java
import com.springnexttodo.category.Category;
```

Add field after `private User user;`:
```java
@ManyToOne(fetch = FetchType.LAZY, optional = true)
@JoinColumn(name = "category_id")
private Category category;
```

Add getter and setter after `public void setUser(User user) { this.user = user; }`:
```java
public Category getCategory() { return category; }
public void setCategory(Category category) { this.category = category; }
```

---

## Task 6: Update TaskRequest and TaskResponse DTOs

**Files:**
- Modify: `apps/api/src/main/java/com/springnexttodo/task/dto/TaskRequest.java`
- Modify: `apps/api/src/main/java/com/springnexttodo/task/dto/TaskResponse.java`

- [ ] **Step 1: Add categoryId to TaskRequest**

Replace `apps/api/src/main/java/com/springnexttodo/task/dto/TaskRequest.java`:
```java
package com.springnexttodo.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(
    @NotBlank(message = "title is required")
    @Size(max = 255, message = "title must be at most 255 characters")
    String title,

    @Size(max = 2000, message = "description must be at most 2000 characters")
    String description,

    Long categoryId
) {}
```

- [ ] **Step 2: Add category to TaskResponse**

Replace `apps/api/src/main/java/com/springnexttodo/task/dto/TaskResponse.java`:
```java
package com.springnexttodo.task.dto;

import com.springnexttodo.category.dto.CategoryResponse;
import com.springnexttodo.task.Task;

import java.time.Instant;

public record TaskResponse(
    Long id,
    String title,
    String description,
    boolean completed,
    Instant createdAt,
    Instant updatedAt,
    CategoryResponse category
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.isCompleted(),
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getCategory() != null ? CategoryResponse.from(task.getCategory()) : null
        );
    }
}
```

---

## Task 7: Update TaskService and TaskController

**Files:**
- Modify: `apps/api/src/main/java/com/springnexttodo/task/TaskService.java`
- Modify: `apps/api/src/main/java/com/springnexttodo/task/TaskController.java`

- [ ] **Step 1: Rewrite TaskService with category support**

Replace `apps/api/src/main/java/com/springnexttodo/task/TaskService.java`:
```java
package com.springnexttodo.task;

import com.springnexttodo.auth.User;
import com.springnexttodo.category.CategoryService;
import com.springnexttodo.task.dto.TaskRequest;
import com.springnexttodo.task.dto.TaskResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository repository;
    private final CategoryService categoryService;

    public TaskService(TaskRepository repository, CategoryService categoryService) {
        this.repository = repository;
        this.categoryService = categoryService;
    }

    public List<TaskResponse> findAll(User user, Long categoryId) {
        if (categoryId != null) {
            var category = categoryService.getEntityById(categoryId, user);
            return repository.findByUserAndCategoryOrderByCreatedAtDesc(user, category)
                    .stream()
                    .map(TaskResponse::from)
                    .toList();
        }
        return repository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse findById(Long id, User user) {
        return TaskResponse.from(getOrThrow(id, user));
    }

    @Transactional
    public TaskResponse create(TaskRequest req, User user) {
        var task = new Task();
        task.setTitle(req.title());
        task.setDescription(req.description());
        task.setUser(user);
        if (req.categoryId() != null) {
            task.setCategory(categoryService.getEntityById(req.categoryId(), user));
        }
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest req, User user) {
        var task = getOrThrow(id, user);
        task.setTitle(req.title());
        task.setDescription(req.description());
        task.setCategory(req.categoryId() != null
                ? categoryService.getEntityById(req.categoryId(), user)
                : null);
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public TaskResponse toggle(Long id, User user) {
        var task = getOrThrow(id, user);
        task.setCompleted(!task.isCompleted());
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public void delete(Long id, User user) {
        var task = getOrThrow(id, user);
        repository.deleteById(task.getId());
    }

    private Task getOrThrow(Long id, User user) {
        return repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + id));
    }
}
```

Note: `update` explicitly sets `category = null` when `categoryId` is null — this lets the frontend remove a category from a task.

- [ ] **Step 2: Update TaskController list() to accept categoryId**

In `apps/api/src/main/java/com/springnexttodo/task/TaskController.java`, replace the `list` method:
```java
@GetMapping
public List<TaskResponse> list(@RequestParam(required = false) Long categoryId, Authentication auth) {
    return service.findAll(currentUser(auth), categoryId);
}
```

---

## Task 8: Update TaskServiceTest

**Files:**
- Modify: `apps/api/src/test/java/com/springnexttodo/task/TaskServiceTest.java`

Adding `@Mock CategoryService categoryService` is required because `TaskService` now takes `CategoryService` in its constructor. Without it, `@InjectMocks` cannot build the service. Existing `TaskRequest` constructors must also add the new `categoryId` arg (`null`).

- [ ] **Step 1: Replace TaskServiceTest**

`apps/api/src/test/java/com/springnexttodo/task/TaskServiceTest.java`:
```java
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
    }

    @Test
    void findAll_with_unknown_category_throws() {
        when(categoryService.getEntityById(99L, user))
                .thenThrow(new EntityNotFoundException("Category not found: 99"));

        assertThatThrownBy(() -> taskService.findAll(user, 99L))
                .isInstanceOf(EntityNotFoundException.class);
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
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_task_of_other_user() {
        when(repository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(99L, user))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
```

- [ ] **Step 2: Run TaskServiceTest**

```bash
cd apps/api && ./mvnw test -Dtest=TaskServiceTest
```

Expected: all 9 tests pass.

---

## Task 9: Update SeedData and run all backend tests

**Files:**
- Modify: `apps/api/src/main/java/com/springnexttodo/config/SeedData.java`

- [ ] **Step 1: Replace SeedData**

`apps/api/src/main/java/com/springnexttodo/config/SeedData.java`:
```java
package com.springnexttodo.config;

import com.springnexttodo.auth.User;
import com.springnexttodo.auth.UserRepository;
import com.springnexttodo.category.Category;
import com.springnexttodo.category.CategoryRepository;
import com.springnexttodo.task.Task;
import com.springnexttodo.task.TaskRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SeedData implements ApplicationRunner {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedData(TaskRepository taskRepository, UserRepository userRepository,
                    CategoryRepository categoryRepository, PasswordEncoder passwordEncoder) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) return;

        User seed = new User();
        seed.setName("Seed User");
        seed.setEmail("seed@todo.dev");
        seed.setPasswordHash(passwordEncoder.encode("seed123"));
        userRepository.save(seed);

        Category trabalho = new Category();
        trabalho.setName("Trabalho");
        trabalho.setColor("#3b82f6");
        trabalho.setUser(seed);
        categoryRepository.save(trabalho);

        Category estudo = new Category();
        estudo.setName("Estudo");
        estudo.setColor("#10b981");
        estudo.setUser(seed);
        categoryRepository.save(estudo);

        Category pessoal = new Category();
        pessoal.setName("Pessoal");
        pessoal.setColor("#f59e0b");
        pessoal.setUser(seed);
        categoryRepository.save(pessoal);

        Task t1 = new Task();
        t1.setTitle("Estudar Spring Boot");
        t1.setDescription("Entender camadas Controller → Service → Repository");
        t1.setUser(seed);
        t1.setCategory(estudo);

        Task t2 = new Task();
        t2.setTitle("Configurar Next.js com shadcn/ui");
        t2.setUser(seed);
        t2.setCategory(trabalho);

        Task t3 = new Task();
        t3.setTitle("Conectar front ao back via fetch");
        t3.setCompleted(true);
        t3.setUser(seed);

        taskRepository.save(t1);
        taskRepository.save(t2);
        taskRepository.save(t3);
    }
}
```

- [ ] **Step 2: Run all backend tests**

```bash
cd apps/api && ./mvnw test
```

Expected: all tests pass — `CategoryServiceTest` (6), `TaskServiceTest` (9), `AuthServiceTest` (4), `JwtServiceTest` (N), `ApiApplicationTests` (1).

- [ ] **Step 3: Commit all backend changes**

```bash
git add apps/api/src
git commit -m "feat(api): extend tasks with category — filter, assign, seed"
```

---

## Task 10: Update api.ts

**Files:**
- Modify: `apps/web/src/lib/api.ts`

- [ ] **Step 1: Replace api.ts**

`apps/web/src/lib/api.ts`:
```typescript
const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export interface Category {
  id: number;
  name: string;
  color: string;
}

export interface Task {
  id: number;
  title: string;
  description: string | null;
  completed: boolean;
  createdAt: string;
  updatedAt: string;
  category: Category | null;
}

export interface User {
  id: number;
  name: string;
  email: string;
}

export interface ApiError {
  status: number;
  message: string;
  errors: string[];
  timestamp: string;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    ...init,
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error((body as ApiError).message ?? `HTTP ${res.status}`);
  }
  if (res.status === 204) return undefined as T;
  return res.json();
}

export const api = {
  tasks: {
    list: () => request<Task[]>("/tasks"),
    get: (id: number) => request<Task>(`/tasks/${id}`),
    create: (data: { title: string; description?: string; categoryId?: number }) =>
      request<Task>("/tasks", { method: "POST", body: JSON.stringify(data) }),
    update: (id: number, data: { title: string; description?: string; categoryId?: number | null }) =>
      request<Task>(`/tasks/${id}`, { method: "PUT", body: JSON.stringify(data) }),
    toggle: (id: number) =>
      request<Task>(`/tasks/${id}/toggle`, { method: "PATCH" }),
    delete: (id: number) =>
      request<void>(`/tasks/${id}`, { method: "DELETE" }),
  },
  auth: {
    register: (data: { name: string; email: string; password: string }) =>
      request<User>("/auth/register", { method: "POST", body: JSON.stringify(data) }),
    login: (data: { email: string; password: string }) =>
      request<User>("/auth/login", { method: "POST", body: JSON.stringify(data) }),
    logout: () =>
      request<void>("/auth/logout", { method: "POST" }),
    me: () =>
      request<User>("/auth/me"),
  },
  categories: {
    list: () => request<Category[]>("/categories"),
    create: (data: { name: string; color: string }) =>
      request<Category>("/categories", { method: "POST", body: JSON.stringify(data) }),
    update: (id: number, data: { name: string; color: string }) =>
      request<Category>(`/categories/${id}`, { method: "PUT", body: JSON.stringify(data) }),
    delete: (id: number) =>
      request<void>(`/categories/${id}`, { method: "DELETE" }),
  },
};
```

---

## Task 11: Create CategoryBadge and CategorySelector

**Files:**
- Create: `apps/web/src/components/tasks/CategoryBadge.tsx`
- Create: `apps/web/src/components/tasks/CategorySelector.tsx`

- [ ] **Step 1: Create CategoryBadge**

`apps/web/src/components/tasks/CategoryBadge.tsx`:
```tsx
"use client";

import { Badge } from "@/components/ui/badge";
import { Category } from "@/lib/api";

interface CategoryBadgeProps {
  category: Category;
}

export function CategoryBadge({ category }: CategoryBadgeProps) {
  return (
    <Badge
      variant="outline"
      className="text-xs gap-1.5 font-normal"
      style={{ borderColor: category.color, color: category.color }}
    >
      <span
        className="inline-block h-2 w-2 rounded-full shrink-0"
        style={{ backgroundColor: category.color }}
      />
      {category.name}
    </Badge>
  );
}
```

- [ ] **Step 2: Create CategorySelector**

`apps/web/src/components/tasks/CategorySelector.tsx`:
```tsx
"use client";

import { Category } from "@/lib/api";

interface CategorySelectorProps {
  categories: Category[];
  value: number | null;
  onChange: (categoryId: number | null) => void;
  disabled?: boolean;
}

export function CategorySelector({ categories, value, onChange, disabled }: CategorySelectorProps) {
  return (
    <select
      className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
      value={value ?? ""}
      onChange={(e) => onChange(e.target.value ? Number(e.target.value) : null)}
      disabled={disabled}
      aria-label="Categoria"
    >
      <option value="">Sem categoria</option>
      {categories.map((cat) => (
        <option key={cat.id} value={cat.id}>
          {cat.name}
        </option>
      ))}
    </select>
  );
}
```

---

## Task 12: Update TaskItem

**Files:**
- Modify: `apps/web/src/components/tasks/TaskItem.tsx`

- [ ] **Step 1: Replace TaskItem**

`apps/web/src/components/tasks/TaskItem.tsx`:
```tsx
"use client";

import { useState } from "react";
import { Checkbox } from "@/components/ui/checkbox";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Pencil, Trash2 } from "lucide-react";
import { Task, Category, api } from "@/lib/api";
import { TaskEditDialog } from "./TaskEditDialog";
import { CategoryBadge } from "./CategoryBadge";
import { cn } from "@/lib/utils";

interface TaskItemProps {
  task: Task;
  categories: Category[];
  onUpdate: (task: Task) => void;
  onDelete: (id: number) => void;
}

export function TaskItem({ task, categories, onUpdate, onDelete }: TaskItemProps) {
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(false);

  async function handleToggle() {
    setLoading(true);
    try {
      const updated = await api.tasks.toggle(task.id);
      onUpdate(updated);
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete() {
    setLoading(true);
    try {
      await api.tasks.delete(task.id);
      onDelete(task.id);
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <div className="flex items-start gap-3 p-4 rounded-xl border border-border bg-card hover:bg-card/80 transition-colors">
        <Checkbox
          checked={task.completed}
          onCheckedChange={handleToggle}
          disabled={loading}
          className="mt-0.5 shrink-0"
          aria-label={task.completed ? "Marcar como pendente" : "Marcar como concluída"}
        />

        <div className="flex-1 min-w-0">
          <p className={cn("font-medium leading-snug break-words", task.completed && "line-through text-muted-foreground")}>
            {task.title}
          </p>
          {task.description && (
            <p className="text-sm text-muted-foreground mt-0.5 break-words">{task.description}</p>
          )}
          {task.category && (
            <div className="mt-1.5">
              <CategoryBadge category={task.category} />
            </div>
          )}
        </div>

        <div className="flex items-center gap-1 shrink-0">
          {task.completed && (
            <Badge variant="secondary" className="text-xs hidden sm:inline-flex">Concluída</Badge>
          )}
          <Button
            variant="ghost"
            size="icon"
            className="h-8 w-8 text-muted-foreground hover:text-foreground"
            onClick={() => setEditing(true)}
            disabled={loading}
            aria-label="Editar"
          >
            <Pencil className="h-4 w-4" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            className="h-8 w-8 text-muted-foreground hover:text-destructive"
            onClick={handleDelete}
            disabled={loading}
            aria-label="Deletar"
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      </div>

      <TaskEditDialog
        key={task.id}
        task={task}
        categories={categories}
        open={editing}
        onOpenChange={setEditing}
        onSave={onUpdate}
      />
    </>
  );
}
```

---

## Task 13: Update TaskForm

**Files:**
- Modify: `apps/web/src/components/tasks/TaskForm.tsx`

- [ ] **Step 1: Replace TaskForm**

`apps/web/src/components/tasks/TaskForm.tsx`:
```tsx
"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Plus } from "lucide-react";
import { Task, Category, api } from "@/lib/api";
import { CategorySelector } from "./CategorySelector";

interface TaskFormProps {
  onCreated: (task: Task) => void;
  categories: Category[];
}

export function TaskForm({ onCreated, categories }: TaskFormProps) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [expanded, setExpanded] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!title.trim()) { setError("Título obrigatório"); return; }
    setLoading(true);
    try {
      const task = await api.tasks.create({
        title: title.trim(),
        description: description.trim() || undefined,
        categoryId: categoryId ?? undefined,
      });
      onCreated(task);
      setTitle("");
      setDescription("");
      setCategoryId(null);
      setExpanded(false);
      setError("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao criar tarefa");
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-2">
      <div className="flex gap-2">
        <Input
          placeholder="Nova tarefa…"
          value={title}
          onChange={(e) => { setTitle(e.target.value); setError(""); if (!expanded && e.target.value) setExpanded(true); }}
          disabled={loading}
          className="flex-1"
          aria-label="Título da tarefa"
        />
        <Button type="submit" disabled={loading || !title.trim()} size="icon" aria-label="Adicionar tarefa">
          <Plus className="h-4 w-4" />
        </Button>
      </div>

      {expanded && (
        <>
          <Input
            placeholder="Descrição (opcional)"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            disabled={loading}
            aria-label="Descrição da tarefa"
          />
          {categories.length > 0 && (
            <CategorySelector
              categories={categories}
              value={categoryId}
              onChange={setCategoryId}
              disabled={loading}
            />
          )}
        </>
      )}

      {error && <p className="text-sm text-destructive">{error}</p>}
    </form>
  );
}
```

---

## Task 14: Update TaskEditDialog

**Files:**
- Modify: `apps/web/src/components/tasks/TaskEditDialog.tsx`

- [ ] **Step 1: Replace TaskEditDialog**

`apps/web/src/components/tasks/TaskEditDialog.tsx`:
```tsx
"use client";

import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Task, Category, api } from "@/lib/api";
import { CategorySelector } from "./CategorySelector";

interface TaskEditDialogProps {
  task: Task;
  categories: Category[];
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSave: (task: Task) => void;
}

export function TaskEditDialog({ task, categories, open, onOpenChange, onSave }: TaskEditDialogProps) {
  const [title, setTitle] = useState(task.title);
  const [description, setDescription] = useState(task.description ?? "");
  const [categoryId, setCategoryId] = useState<number | null>(task.category?.id ?? null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSave() {
    if (!title.trim()) { setError("Título obrigatório"); return; }
    setLoading(true);
    try {
      const updated = await api.tasks.update(task.id, {
        title: title.trim(),
        description: description.trim() || undefined,
        categoryId: categoryId,
      });
      onSave(updated);
      onOpenChange(false);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Erro ao salvar");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Editar tarefa</DialogTitle>
        </DialogHeader>
        <div className="flex flex-col gap-3 py-2">
          <Input
            placeholder="Título *"
            value={title}
            onChange={(e) => { setTitle(e.target.value); setError(""); }}
            disabled={loading}
            autoFocus
          />
          <Input
            placeholder="Descrição (opcional)"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            disabled={loading}
          />
          {categories.length > 0 && (
            <CategorySelector
              categories={categories}
              value={categoryId}
              onChange={setCategoryId}
              disabled={loading}
            />
          )}
          {error && <p className="text-sm text-destructive">{error}</p>}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={loading}>Cancelar</Button>
          <Button onClick={handleSave} disabled={loading}>{loading ? "Salvando…" : "Salvar"}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
```

---

## Task 15: Update TaskList

**Files:**
- Modify: `apps/web/src/components/tasks/TaskList.tsx`

- [ ] **Step 1: Replace TaskList**

`apps/web/src/components/tasks/TaskList.tsx`:
```tsx
"use client";

import { useState } from "react";
import { Task, Category } from "@/lib/api";
import { TaskItem } from "./TaskItem";
import { TaskForm } from "./TaskForm";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

interface TaskListProps {
  initialTasks: Task[];
  categories: Category[];
}

export function TaskList({ initialTasks, categories }: TaskListProps) {
  const [tasks, setTasks] = useState<Task[]>(initialTasks);
  const [filterCategoryId, setFilterCategoryId] = useState<number | null>(null);

  function handleCreated(task: Task) {
    setTasks((prev) => [task, ...prev]);
  }

  function handleUpdate(updated: Task) {
    setTasks((prev) => prev.map((t) => (t.id === updated.id ? updated : t)));
  }

  function handleDelete(id: number) {
    setTasks((prev) => prev.filter((t) => t.id !== id));
  }

  const filtered = filterCategoryId != null
    ? tasks.filter((t) => t.category?.id === filterCategoryId)
    : tasks;

  const pending = filtered.filter((t) => !t.completed);
  const done = filtered.filter((t) => t.completed);

  return (
    <div className="flex flex-col gap-6">
      <TaskForm onCreated={handleCreated} categories={categories} />

      {categories.length > 0 && (
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            onClick={() => setFilterCategoryId(null)}
            className={cn(
              "px-3 py-1 text-xs rounded-full border transition-colors",
              filterCategoryId === null
                ? "bg-primary text-primary-foreground border-primary"
                : "border-border text-muted-foreground hover:text-foreground"
            )}
          >
            Todas
          </button>
          {categories.map((cat) => (
            <button
              key={cat.id}
              type="button"
              onClick={() => setFilterCategoryId(cat.id === filterCategoryId ? null : cat.id)}
              className={cn(
                "px-3 py-1 text-xs rounded-full border transition-colors",
                filterCategoryId === cat.id
                  ? "text-white border-transparent"
                  : "border-border text-muted-foreground hover:text-foreground"
              )}
              style={filterCategoryId === cat.id ? { backgroundColor: cat.color, borderColor: cat.color } : {}}
            >
              {cat.name}
            </button>
          ))}
        </div>
      )}

      {filtered.length === 0 && (
        <p className="text-center text-muted-foreground text-sm py-8">
          {filterCategoryId != null
            ? "Nenhuma tarefa nesta categoria."
            : "Nenhuma tarefa ainda. Adicione a primeira acima!"}
        </p>
      )}

      {pending.length > 0 && (
        <section>
          <div className="flex items-center gap-2 mb-3">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">Pendentes</h2>
            <Badge variant="outline">{pending.length}</Badge>
          </div>
          <div className="flex flex-col gap-2">
            {pending.map((task) => (
              <TaskItem key={task.id} task={task} categories={categories} onUpdate={handleUpdate} onDelete={handleDelete} />
            ))}
          </div>
        </section>
      )}

      {done.length > 0 && (
        <section>
          <div className="flex items-center gap-2 mb-3">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">Concluídas</h2>
            <Badge variant="outline">{done.length}</Badge>
          </div>
          <div className="flex flex-col gap-2">
            {done.map((task) => (
              <TaskItem key={task.id} task={task} categories={categories} onUpdate={handleUpdate} onDelete={handleDelete} />
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
```

---

## Task 16: Update page.tsx and run frontend checks

**Files:**
- Modify: `apps/web/src/app/page.tsx`

- [ ] **Step 1: Replace page.tsx**

`apps/web/src/app/page.tsx`:
```tsx
import { Task, Category, User } from "@/lib/api";
import { TaskList } from "@/components/tasks/TaskList";
import { LogoutButton } from "@/components/auth/LogoutButton";
import { CheckSquare } from "lucide-react";
import { cookies } from "next/headers";

export const dynamic = "force-dynamic";

async function getTasks(authCookie: string | undefined): Promise<Task[]> {
  if (!authCookie) return [];
  try {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
    const res = await fetch(`${apiUrl}/tasks`, {
      headers: { Cookie: `auth_token=${authCookie}` },
      cache: "no-store",
    });
    return res.ok ? res.json() : [];
  } catch {
    return [];
  }
}

async function getCategories(authCookie: string | undefined): Promise<Category[]> {
  if (!authCookie) return [];
  try {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
    const res = await fetch(`${apiUrl}/categories`, {
      headers: { Cookie: `auth_token=${authCookie}` },
      cache: "no-store",
    });
    return res.ok ? res.json() : [];
  } catch {
    return [];
  }
}

async function getCurrentUser(authCookie: string | undefined): Promise<User | null> {
  if (!authCookie) return null;
  try {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
    const res = await fetch(`${apiUrl}/auth/me`, {
      headers: { Cookie: `auth_token=${authCookie}` },
      cache: "no-store",
    });
    return res.ok ? res.json() : null;
  } catch {
    return null;
  }
}

export default async function HomePage() {
  const cookieStore = await cookies();
  const authCookie = cookieStore.get("auth_token")?.value;

  const [tasks, categories, user] = await Promise.all([
    getTasks(authCookie),
    getCategories(authCookie),
    getCurrentUser(authCookie),
  ]);

  return (
    <main className="flex-1 flex flex-col">
      <header className="border-b border-border bg-card/50 backdrop-blur-sm sticky top-0 z-10">
        <div className="max-w-xl mx-auto px-4 py-4 flex items-center gap-2">
          <CheckSquare className="h-5 w-5 text-primary" />
          <h1 className="font-bold text-lg tracking-tight">spring-next-todo</h1>
          <span className="ml-auto text-sm text-muted-foreground">{user?.name}</span>
          <LogoutButton />
        </div>
      </header>

      <div className="flex-1 max-w-xl mx-auto w-full px-4 py-6">
        <TaskList initialTasks={tasks} categories={categories} />
      </div>
    </main>
  );
}
```

- [ ] **Step 2: Run lint**

```bash
cd apps/web && npm run lint
```

Expected: no errors, 0 warnings.

- [ ] **Step 3: Commit frontend changes**

```bash
git add apps/web/src
git commit -m "feat(web): category badge, selector, and filter UI"
```
