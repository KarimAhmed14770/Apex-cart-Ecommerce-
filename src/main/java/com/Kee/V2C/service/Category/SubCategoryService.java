package com.Kee.V2C.service.Category;

import com.Kee.V2C.dto.category.SubCategoryRegisterRequest;
import com.Kee.V2C.dto.category.SubCategoryResponse;
import com.Kee.V2C.dto.category.SubCategoryUpdateRequest;
import com.Kee.V2C.entity.SubCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubCategoryService {
    SubCategoryResponse addSubCategory(Long parentId, SubCategoryRegisterRequest subCategoryRegisterRequest);
    SubCategoryResponse updateSubCategory(Long id, SubCategoryUpdateRequest subCategoryRequest);
    SubCategoryResponse softDeleteSubCategory(Long id);
    SubCategoryResponse getSubCategoryById(Long id);
    Page<SubCategoryResponse> getActiveSubCategoriesOfParent(Long parentId, Pageable page);

}
