package com.eticaret.product.domain;

import com.eticaret.product.dto.CategoryResponse;
import com.eticaret.product.dto.CreateCategoryRequest;
import com.eticaret.product.exception.CategoryNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse getCategoryById(Long id) {
        return CategoryResponse.from(categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id)));
    }
}