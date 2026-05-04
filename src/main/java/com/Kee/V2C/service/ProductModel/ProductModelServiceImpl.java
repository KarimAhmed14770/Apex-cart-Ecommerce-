package com.Kee.V2C.service.ProductModel;

import com.Kee.V2C.Repository.*;
import com.Kee.V2C.dto.product.*;
import com.Kee.V2C.entity.*;
import com.Kee.V2C.enums.PathFolder;
import com.Kee.V2C.enums.ProductModelStatus;
import com.Kee.V2C.enums.ProductRequestStatus;
import com.Kee.V2C.exception.ResourceNotFoundException;
import com.Kee.V2C.mapper.ProductMapper;
import com.Kee.V2C.mapper.ProductModelMapper;
import com.Kee.V2C.service.Image.ImageService;
import com.Kee.V2C.specifications.ProductModelSpecs;
import com.Kee.V2C.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductModelServiceImpl implements ProductModelService{

    private final ProductModelRepository productModelRepository;
    private final ImageService imageService;
    private final VendorRepository vendorRepository;
    private final BrandRepository brandRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ProductModelMapper productModelMapper;
    private final SecurityUtil securityUtil;

    @Autowired
    public ProductModelServiceImpl(ProductModelRepository productModelRepository, ImageService imageService,
                                   VendorRepository vendorRepository,BrandRepository brandRepository,
                                   SubCategoryRepository subCategoryRepository, ProductModelMapper productModelMapper,
                                   final SecurityUtil securityUtil){
        this.productModelRepository=productModelRepository;
        this.imageService=imageService;
        this.vendorRepository=vendorRepository;
        this.brandRepository=brandRepository;
        this.subCategoryRepository=subCategoryRepository;
        this.productModelMapper=productModelMapper;
        this.securityUtil=securityUtil;

    }




    @Override
    @Transactional
    public ProductModelResponse addProductModel(ProductModelRegisterRequest productModelRegisterRequest){
        ProductModel productModel=convertProductModelRequestToProductModel(productModelRegisterRequest);
        productModelRepository.save(productModel);
        return convertProductModelToDto(productModel);
    }

    @Override
    @Transactional
    public ProductModelResponse updateProductModel(Long id, ProductModelUpdateRequest productModelUpdateRequest){
        ProductModel productModel=productModelRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Product Model with id: "+id+" not found"));

        productModelMapper.updateProductModelFromDto(productModelUpdateRequest,productModel);
        if(productModelUpdateRequest.image()!=null && !productModelUpdateRequest.image().isEmpty()) {
            String updated_img = imageService.saveImage(productModelUpdateRequest.image(), PathFolder.MODELS);
            productModel.setImageUrl(updated_img);
        }
        productModelRepository.save(productModel);

        return convertProductModelToDto(productModel);
    }

    @Override
    @Transactional
    public ProductModelResponse softDeleteProductModel(Long id){
        ProductModel productModel=productModelRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Product Model with id: "+id+" not found"));

        productModel.setStatus(ProductModelStatus.DISABLED);
        productModelRepository.save(productModel);

        return convertProductModelToDto(productModel);

    }

    public Page<ProductModelResponse> searchProductModel(String name, String description, Long ownerId,
                                                         Long subCategoryId, Long brandId, Boolean isGlobal,
                                                         ProductModelStatus status, Pageable page) {
        Page<ProductModel> productModels=getProductModelsByAttributes(name, description, ownerId,
                subCategoryId, brandId, isGlobal, status, page);
        return productModels.map(this::convertProductModelToDto);
    }


    @Override
    public ProductModel getProductModelById(Long id){
        return productModelRepository.findById(id).
                orElseThrow(()->new ResourceNotFoundException("product model with id: "+id+" not found."));
    }

    @Override
    public Page<ProductModel> getActiveProductModels(Pageable page){
        return getProductModelsByAttributes(null,null,null,null,null
        ,null,ProductModelStatus.ACTIVE,page);
    }

    @Override
    public Page<ProductModel> getProductModelsByAttributes(String name, String description,
                                                    Long ownerId, Long subCategoryId, Long brandId,
                                                    Boolean isGlobal, ProductModelStatus status,
                                                    Pageable page){
        Specification<ProductModel> spec=Specification
                .where((root, query, cb) -> cb.conjunction() );

        if(name!=null && !(name.isEmpty()))spec=spec.and(ProductModelSpecs.hasName(name));
        if(description!=null && !(description.isEmpty()))spec=spec.and(ProductModelSpecs.hasDescription(description));
        if(ownerId!=null)spec=spec.and(ProductModelSpecs.hasVendor(ownerId));
        if(isGlobal!=null)spec=spec.and(ProductModelSpecs.isGlobal(isGlobal));
        if(subCategoryId!=null)spec=spec.and(ProductModelSpecs.hasSubCategory(subCategoryId));
        if(brandId!=null)spec=spec.and(ProductModelSpecs.hasBrand(brandId));
        if(status!=null)spec=spec.and(ProductModelSpecs.hasStatus(status));

        Page<ProductModel> productModels=productModelRepository.findAll(spec,page);

        return productModels;
    }

    @Override
    public Page<ProductModelResponse> getMyProductModels(String description, Long subCategoryId, Long brandId, Pageable page) {
        Long vendorId = securityUtil.getCurrentUserId();
        return searchProductModel(null, description, vendorId, subCategoryId, brandId, null, ProductModelStatus.ACTIVE, page);
    }

    public ProductModelResponse convertProductModelToDto(ProductModel productModel){
        return new ProductModelResponse(
                productModel.getId(),
                (productModel.getBrand()==null?null:productModel.getBrand().getId()),
                productModel.getSubCategory().getId(),
                (productModel.getVendor()==null)?null:productModel.getVendor().getId(),
                productModel.isGlobal(),
                productModel.getName(),
                productModel.getDescription(),
                productModel.getImageUrl(),
                productModel.getStatus()
        );
    }


    private ProductModel convertProductModelRequestToProductModel(ProductModelRegisterRequest productModelRegisterRequest){
        Brand brand=brandRepository.findById(productModelRegisterRequest.brandId()).orElseThrow(
                ()-> new ResourceNotFoundException("Brand with id: "+ productModelRegisterRequest.brandId()+
                        " not found.")
        );
        SubCategory category=subCategoryRepository.findById(productModelRegisterRequest.subCategoryId()).orElseThrow(
                ()->new ResourceNotFoundException("category with id: "+ productModelRegisterRequest.subCategoryId()+
                        " is not found.")
        );
        Vendor vendor=null;
        if(!productModelRegisterRequest.isGlobal()){
            vendor=vendorRepository.findById(productModelRegisterRequest.vendorId()).orElseThrow(
                    ()->new ResourceNotFoundException("vendor with id: "+ productModelRegisterRequest.vendorId()+
                            " is not found.")
            );
        }
        ProductModel productModel = new ProductModel(
                productModelRegisterRequest.name(),
                productModelRegisterRequest.description(),
                imageService.saveImage(productModelRegisterRequest.image(), PathFolder.MODELS),
                vendor,
                productModelRegisterRequest.isGlobal(),
                productModelRegisterRequest.status(),
                brand,
                category
        );
        brand.addProductModel(productModel);
        category.addProductModel(productModel);
        if(vendor!=null){
            vendor.addProductModel(productModel);
        }
        return productModel;
    }




}
