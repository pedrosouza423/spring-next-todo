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
    Optional<Task> findByIdAndUser(Long id, User user);

    @Query("""
        SELECT t FROM Task t
        WHERE t.user = :user
          AND (:categoryId IS NULL OR t.category.id = :categoryId)
          AND (:priority   IS NULL OR t.priority   = :priority)
          AND (:completed  IS NULL OR t.completed  = :completed)
          AND (:q IS NULL
               OR LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')) ESCAPE '!'
               OR LOWER(COALESCE(t.description, '')) LIKE LOWER(CONCAT('%', :q, '%')) ESCAPE '!')
        ORDER BY t.createdAt DESC
    """)
    List<Task> findFiltered(
        @Param("user") User user,
        @Param("categoryId") Long categoryId,
        @Param("priority") Priority priority,
        @Param("completed") Boolean completed,
        @Param("q") String q
    );

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Task t SET t.category = null WHERE t.category = :category")
    void clearCategory(@Param("category") Category category);
}
