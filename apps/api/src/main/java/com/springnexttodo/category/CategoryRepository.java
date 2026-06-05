package com.springnexttodo.category;

import com.springnexttodo.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserOrderByName(User user);
    Optional<Category> findByIdAndUser(Long id, User user);
}
