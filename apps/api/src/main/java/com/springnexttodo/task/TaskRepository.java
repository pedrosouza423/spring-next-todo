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
