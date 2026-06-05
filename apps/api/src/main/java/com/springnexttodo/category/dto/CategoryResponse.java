package com.springnexttodo.category.dto;

import com.springnexttodo.category.Category;

public record CategoryResponse(Long id, String name, String color) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getColor());
    }
}
