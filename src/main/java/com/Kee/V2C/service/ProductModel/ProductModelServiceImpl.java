package com.Kee.V2C.service.ProductModel;

import com.Kee.V2C.Repository.ProductModelRepository;
import com.Kee.V2C.Repository.ProductRequestRepository;
import com.Kee.V2C.Repository.VendorRepository;
import com.Kee.V2C.dto.product.NewProductRequest;
import com.Kee.V2C.dto.product.ProductModelResponse;
import com.Kee.V2C.dto.product.ProductRequestResponse;
import com.Kee.V2C.entity.ProductModel;
import com.Kee.V2C.entity.ProductRequest;
import com.Kee.V2C.entity.Vendor;
import com.Kee.V2C.enums.PathFolder;
import com.Kee.V2C.enums.ProductModelStatus;
import com.Kee.V2C.enums.ProductRequestStatus;
import com.Kee.V2C.exception.ResourceNotFoundException;
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
    private final ProductRequestRepository productRequestRepository;
    private final ImageService imageService;
    private final SecurityUtil securityUtil;
    private final VendorRepository vendorRepository;
    @Autowired
    public ProductModelServiceImpl(ProductModelRepository productModelRepository,ProductRequestRepository productRequestRepository,
                                   ImageService imageService,SecurityUtil securityUtil,
                                   VendorRepository vendorRepository){
        this.productModelRepository=productModelRepository;
        this.productRequestRepository=productRequestRepository;
        this.imageService=imageService;
        this.securityUtil=securityUtil;
        this.vendorRepository=vendorRepository;

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
        if(subCategoryId!=null)spec=spec.and(ProductModelSpecs.hasSubCategory(subCategoryId));
        if(brandId!=null)spec=spec.and(ProductModelSpecs.hasBrand(brandId));
        if(isGlobal!=null)spec=spec.and(ProductModelSpecs.isGlobal(isGlobal));
        if(status!=null)spec=spec.and(ProductModelSpecs.hasStatus(status));

        Page<ProductModel> productModels=productModelRepository.findAll(spec,page);

        return productModels;
    }

    @Override
    @Transactional
    public ProductRequestResponse requestNewProduct(NewProductRequest newProductRequest){
        Vendor vendor=getCurrentVendor();
        ProductRequest productRequest=new ProductRequest(newProductRequest.name(), newProductRequest.description(),
                imageService.saveImage(newProductRequest.imageFile(), PathFolder.PRODUCT_REQUESTS), newProductRequest.isGlobal(), ProductRequestStatus.PENDING,vendor);
        productRequestRepository.save(productRequest);
        return new ProductRequestResponse(
                productRequest.getId(), productRequest.getName(), productRequest.getDescription(),
                productRequest.getImageUrl(), productRequest.getGlobal(),productRequest.getStatus()
        );

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

    private Vendor getCurrentVendor(){
        Long userId=securityUtil.getCurrentUserId();
        Vendor vendor= vendorRepository.findById(userId)
                .orElseThrow(()->new UsernameNotFoundException("Seller with id: "
                        +userId+"does not exist"));
        return vendor;
    }
}
