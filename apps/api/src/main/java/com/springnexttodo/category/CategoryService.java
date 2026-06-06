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

    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;

    public CategoryService(CategoryRepository categoryRepository, TaskRepository taskRepository) {
        this.categoryRepository = categoryRepository;
        this.taskRepository = taskRepository;
    }

    public List<CategoryResponse> findAll(User user) {
        return categoryRepository.findByUserOrderByName(user)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public Category getEntityById(Long id, User user) {
        return categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req, User user) {
        var category = new Category();
        category.setName(req.name());
        category.setColor(req.color());
        category.setUser(user);
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest req, User user) {
        var category = getEntityById(id, user);
        category.setName(req.name());
        category.setColor(req.color());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id, User user) {
        var category = getEntityById(id, user);
        taskRepository.clearCategory(category);
        categoryRepository.delete(category);
    }
}
