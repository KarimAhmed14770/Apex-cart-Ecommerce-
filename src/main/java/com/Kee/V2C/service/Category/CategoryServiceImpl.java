package com.Kee.V2C.service.Category;

import com.Kee.V2C.Repository.CategoryRepository;
import com.Kee.V2C.dto.category.CategoryRegisterRequest;
import com.Kee.V2C.dto.category.CategoryResponse;
import com.Kee.V2C.dto.category.CategoryUpdateRequest;
import com.Kee.V2C.entity.Category;
import com.Kee.V2C.enums.PathFolder;
import com.Kee.V2C.exception.CategoryNotFoundException;
import com.Kee.V2C.exception.ResourceAlreadyExistsException;
import com.Kee.V2C.mapper.CategoryMapper;
import com.Kee.V2C.service.Image.ImageService;
import com.Kee.V2C.specifications.CategorySpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ImageService imageService;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository,CategoryMapper categoryMapper,
                               ImageService imageService){
        this.categoryRepository=categoryRepository;
        this.categoryMapper=categoryMapper;
        this.imageService=imageService;
    }

    @Override
    public Page<CategoryResponse> getCategoryByAttribute(String name,String description,
                                                         Boolean active,Pageable page){
        Specification<Category> spec=(root, query, cb) -> cb.conjunction() ;
        if(name!=null) spec=spec.and(CategorySpecs.hasName(name));
        if(description!=null) spec=spec.and(CategorySpecs.hasDescription(description));
        if(active!=null) spec=spec.and(CategorySpecs.hasActive(active));

        Page<Category> categories=categoryRepository.findAll(spec,page);

        return  categories.map(this::convertCategoryToDto);
    }

    @Override
    public CategoryResponse getCategoryById(Long id){
        return convertCategoryToDto(categoryRepository.findById(id).orElseThrow(
                ()->new CategoryNotFoundException("Category with id: "+id+" Not found.")));
    }

    @Override
    public Page<CategoryResponse> getActiveCategories(Pageable page){
        return categoryRepository.findByActiveTrue(page).map(this::convertCategoryToDto);

    }

    @Override
    @Transactional
    public CategoryResponse addCategory(CategoryRegisterRequest categoryRegisterRequest) {
        if(categoryRepository.existsByNameIgnoreCase(categoryRegisterRequest.name())){
            throw new ResourceAlreadyExistsException("category Already exists ");
        }
        Category category=new Category(categoryRegisterRequest.name(), categoryRegisterRequest.description(),
                imageService.saveImage(categoryRegisterRequest.imageFile(), PathFolder.CATEGORIES),
                categoryRegisterRequest.active());
        categoryRepository.save(category);

        return convertCategoryToDto(category);
    }



    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest categoryRequest){
        Category updatedCategory=getCategoryByIdHelper(id);
        categoryMapper.updateCategoryFromDto(categoryRequest,updatedCategory);
        if(categoryRequest.imageFile()!=null && !categoryRequest.imageFile().isEmpty()) {
            String updated_img = imageService.saveImage(categoryRequest.imageFile(), PathFolder.CATEGORIES);
            updatedCategory.setImageUrl(updated_img);
        }
        categoryRepository.save(updatedCategory);

        return convertCategoryToDto(updatedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse softDeleteCategory(Long id){
        Category category=getCategoryByIdHelper(id);
        category.setActive(false);
        categoryRepository.save(category);
        return convertCategoryToDto(category);
    }





    private CategoryResponse convertCategoryToDto(Category category){
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getImageUrl(),
                category.isActive()
        );
    }

    private Category getCategoryByIdHelper(Long id){
        return categoryRepository.findById(id).orElseThrow(
                ()->new CategoryNotFoundException("Category with id: "+id+" Not found."));
    }
}

