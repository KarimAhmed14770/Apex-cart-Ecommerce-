package com.Kee.V2C.rest;


import com.Kee.V2C.dto.category.*;
import com.Kee.V2C.service.Category.CategoryService;
import com.Kee.V2C.service.Category.SubCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final SubCategoryService subCategoryService;

    @Autowired
    public CategoryController(CategoryService categoryService, SubCategoryService subCategoryService) {
        this.categoryService = categoryService;
        this.subCategoryService = subCategoryService;
    }



    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryResponse> addCategory(@Valid @ModelAttribute CategoryRegisterRequest categoryRegisterRequest){
        return ResponseEntity.ok(categoryService.addCategory(categoryRegisterRequest));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(value = "/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable("id") Long id,
                                                           @Valid @ModelAttribute CategoryUpdateRequest categoryUpdateRequest){
        return ResponseEntity.ok(categoryService.updateCategory(id,categoryUpdateRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/delete/{id}")
    public ResponseEntity<CategoryResponse> softDelete(@PathVariable("id") Long id){
        return ResponseEntity.ok(categoryService.softDeleteCategory(id));
    }


    @GetMapping("/")
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(Pageable page) {
        return ResponseEntity.ok(categoryService.getActiveCategories(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CategoryResponse>> getCategoryByAttribute(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean active,
            Pageable page) {
        return ResponseEntity.ok(categoryService.getCategoryByAttribute(name, description, active, page));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value="/categories/{id}/subcategories",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubCategoryResponse> addSubCategory(@PathVariable("id") Long id
            , @Valid @ModelAttribute SubCategoryRegisterRequest subCategoryRegisterRequest){
        return ResponseEntity.ok(subCategoryService.addSubCategory(id, subCategoryRegisterRequest));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(value = "{parentId}/subcategories/{subCategoryId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubCategoryResponse> updateSubCategory(@PathVariable("subCategoryId") Long subCategoryId,
                                                                 @Valid @ModelAttribute SubCategoryUpdateRequest subCategoryRequest){
        return ResponseEntity.ok(subCategoryService.updateSubCategory(subCategoryId,subCategoryRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("{parentId}/subcategories/delete/{subCategoryId}")
    public ResponseEntity<SubCategoryResponse> softDeleteSubCategory(@PathVariable("subCategoryId") Long subCategoryId){
        return ResponseEntity.ok(subCategoryService.softDeleteSubCategory(subCategoryId));
    }

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<Page<SubCategoryResponse>> getAllSubCategories(@PathVariable("id") Long id, Pageable page) {
        return ResponseEntity.ok(subCategoryService.getActiveSubCategoriesOfParent(id, page));
    }

    @GetMapping("/{id}/subcategories/{subCategoryId}")
    public ResponseEntity<SubCategoryResponse> getSubCategoryById(@PathVariable("subCategoryId") Long id) {
        return ResponseEntity.ok(subCategoryService.getSubCategoryById(id));


    }


}
