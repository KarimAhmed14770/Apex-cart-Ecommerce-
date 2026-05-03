package com.Kee.V2C.service.Category;

import com.Kee.V2C.dto.category.CategoryRegisterRequest;
import com.Kee.V2C.dto.category.CategoryResponse;
import com.Kee.V2C.dto.category.CategoryUpdateRequest;
import com.Kee.V2C.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    CategoryResponse addCategory(CategoryRegisterRequest categoryRegisterRequest);
    CategoryResponse updateCategory(Long id, CategoryUpdateRequest categoryRequest);
    CategoryResponse softDeleteCategory(Long id);
    Page<CategoryResponse> getCategoryByAttribute(String name,String description,Boolean active,Pageable page);
    CategoryResponse getCategoryById(Long id);
    Page<CategoryResponse> getActiveCategories(Pageable page);
}
