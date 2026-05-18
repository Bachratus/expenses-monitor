package com.bachratus.expenses_monitor.category;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
        }

        String normalizedName = name.trim();
        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
        }

        Category category = new Category(normalizedName);
        return categoryRepository.save(category);
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }
}
