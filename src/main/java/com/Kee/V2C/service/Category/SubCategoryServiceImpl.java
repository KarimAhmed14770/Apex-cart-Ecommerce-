package com.Kee.V2C.service.Category;

import com.Kee.V2C.Repository.CategoryRepository;
import com.Kee.V2C.Repository.SubCategoryRepository;
import com.Kee.V2C.dto.category.SubCategoryRegisterRequest;
import com.Kee.V2C.dto.category.SubCategoryResponse;
import com.Kee.V2C.dto.category.SubCategoryUpdateRequest;
import com.Kee.V2C.entity.Category;
import com.Kee.V2C.entity.SubCategory;
import com.Kee.V2C.enums.PathFolder;
import com.Kee.V2C.exception.ResourceAlreadyExistsException;
import com.Kee.V2C.exception.ResourceNotFoundException;
import com.Kee.V2C.mapper.SubCategoryMapper;
import com.Kee.V2C.service.Image.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubCategoryServiceImpl implements SubCategoryService {

    private final SubCategoryRepository subCategoryRepository;
    private final SubCategoryMapper subCategoryMapper;
    private final ImageService imageService;
    private final CategoryRepository categoryRepository;

    @Autowired
    public SubCategoryServiceImpl(SubCategoryRepository subCategoryRepository,CategoryRepository categoryRepository,
                                  SubCategoryMapper subCategoryMapper,ImageService imageService){
        this.subCategoryRepository=subCategoryRepository;
        this.categoryRepository=categoryRepository;
        this.subCategoryMapper=subCategoryMapper;
        this.imageService=imageService;
    }

    @Override
    public SubCategoryResponse getSubCategoryById(Long id){
        return convertSubCategoryToDto(subCategoryRepository.findById(id).orElseThrow(
                ()->new ResourceNotFoundException("sub-category with id: "+id+" not found.")));

    }

    @Override
    public Page<SubCategoryResponse> getActiveSubCategoriesOfParent(Long parentId, Pageable page){
        return subCategoryRepository.findByParentIdAndActiveTrue(parentId,page).map(this::convertSubCategoryToDto);
    }

    @Override
    @Transactional
    public SubCategoryResponse addSubCategory(Long parentId, SubCategoryRegisterRequest subCategoryRegisterRequest){
        if(subCategoryRepository.existsByNameIgnoreCase(subCategoryRegisterRequest.name())){
            throw new ResourceAlreadyExistsException("category Already exists ");
        }

        Category parentCategory=categoryRepository.findById(parentId)
                .orElseThrow(()-> new ResourceNotFoundException("category with id: "+parentId+ "not found"));
        SubCategory subCategory=new SubCategory(
                parentCategory,
                subCategoryRegisterRequest.name(),
                subCategoryRegisterRequest.description(),
                imageService.saveImage(subCategoryRegisterRequest.imageFile(), PathFolder.SUBCATEGORIES),
                subCategoryRegisterRequest.active());
        parentCategory.addSubcategory(subCategory);//linking parent to sub
        subCategoryRepository.save(subCategory);
        return convertSubCategoryToDto(subCategory);
    }


    @Override
    @Transactional
    public SubCategoryResponse updateSubCategory(Long id, SubCategoryUpdateRequest subCategoryRequest){
        SubCategory updatedCategory=getSubCategoryByIdHelper(id);
        subCategoryMapper.updateSubCategoryFromDto(subCategoryRequest,updatedCategory);
        if(subCategoryRequest.imageFile()!=null && !subCategoryRequest.imageFile().isEmpty()) {
            String updated_img = imageService.saveImage(subCategoryRequest.imageFile(), PathFolder.SUBCATEGORIES);
            updatedCategory.setImageUrl(updated_img);
        }
        subCategoryRepository.save(updatedCategory);

        return convertSubCategoryToDto(updatedCategory);
    }


    @Override
    @Transactional
    public SubCategoryResponse softDeleteSubCategory(Long id){
        SubCategory updatedCategory=getSubCategoryByIdHelper(id);
        updatedCategory.setActive(false);
        subCategoryRepository.save(updatedCategory);
        return convertSubCategoryToDto(updatedCategory);
    }


    private SubCategoryResponse convertSubCategoryToDto(SubCategory subCategory){
        return new SubCategoryResponse(
                subCategory.getParentCategory().getId(),
                subCategory.getId(),
                subCategory.getName(),
                subCategory.getDescription(),
                subCategory.getImageUrl(),
                subCategory.isActive()
        );
    }

    private SubCategory getSubCategoryByIdHelper(Long id){
        return subCategoryRepository.findById(id).orElseThrow(
                ()->new ResourceNotFoundException("sub-category with id: "+id+" not found."));

    }
}

