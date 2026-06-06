package com.springnexttodo.category;

import com.springnexttodo.auth.AuthService;
import com.springnexttodo.common.BaseController;
import com.springnexttodo.category.dto.CategoryRequest;
import com.springnexttodo.category.dto.CategoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController extends BaseController {

    private final CategoryService service;

    public CategoryController(CategoryService service, AuthService authService) {
        super(authService);
        this.service = service;
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
