package com.Kee.V2C.service.productRequest;

import com.Kee.V2C.Repository.*;
import com.Kee.V2C.dto.product.AdminAdditionOnProductRequest;
import com.Kee.V2C.dto.product.NewProductRequest;
import com.Kee.V2C.dto.product.ProductModelResponse;
import com.Kee.V2C.dto.product.ProductRequestResponse;
import com.Kee.V2C.entity.Brand;
import com.Kee.V2C.entity.ProductModel;
import com.Kee.V2C.entity.SubCategory;
import com.Kee.V2C.entity.Vendor;
import com.Kee.V2C.enums.PathFolder;
import com.Kee.V2C.enums.ProductModelStatus;
import com.Kee.V2C.enums.ProductRequestStatus;
import com.Kee.V2C.exception.ResourceAlreadyExistsException;
import com.Kee.V2C.exception.ResourceNotFoundException;
import com.Kee.V2C.service.Image.ImageService;
import com.Kee.V2C.service.ProductModel.ProductModelService;
import com.Kee.V2C.utils.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductRequestServiceImpl implements ProductRequestService{


    private final ProductRequestRepository productRequestRepository;
    private final ImageService imageService;
    private final BrandRepository brandRepository;
    private final ProductModelService productModelService;
    private final ProductModelRepository productModelRepository;
    private final SecurityUtil securityUtil;
    private final VendorRepository vendorRepository;
    private final SubCategoryRepository subCategoryRepository;

    public ProductRequestServiceImpl(ProductRequestRepository productRequestRepository, ImageService imageService,
                                     BrandRepository brandRepository, ProductModelService productModelService,
                                     ProductModelRepository productModelRepository, SecurityUtil securityUtil,
                                     VendorRepository vendorRepository, SubCategoryRepository subCategoryRepository){
        this.productRequestRepository=productRequestRepository;
        this.imageService=imageService;
        this.productModelService=productModelService;
        this.brandRepository=brandRepository;
        this.productModelRepository=productModelRepository;
        this.securityUtil=securityUtil;
        this.vendorRepository=vendorRepository;
        this.subCategoryRepository=subCategoryRepository;

    }

    @Override
    @Transactional
    public ProductRequestResponse requestNewProduct(NewProductRequest newProductRequest){
        Vendor vendor=getCurrentVendor();
        com.Kee.V2C.entity.ProductRequest productRequest=new com.Kee.V2C.entity.ProductRequest(newProductRequest.name(), newProductRequest.description(),
                imageService.saveImage(newProductRequest.imageFile(), PathFolder.PRODUCT_REQUESTS), newProductRequest.isGlobal(), ProductRequestStatus.PENDING,vendor);
        productRequestRepository.save(productRequest);
        return new ProductRequestResponse(
                productRequest.getId(), productRequest.getName(), productRequest.getDescription(),
                productRequest.getImageUrl(), productRequest.getGlobal(),productRequest.getStatus()
        );

    }


    @Override
    @Transactional
    public Page<ProductRequestResponse> getAllProductsRequests(Pageable page){
        Page<com.Kee.V2C.entity.ProductRequest> productRequests=productRequestRepository.findAll(page);
        return productRequests.map(this::convertProductRequestToDto);
    }
    @Override
    public Page<ProductRequestResponse> getPendingVendorsProductsRequests(Pageable page){
        Page<com.Kee.V2C.entity.ProductRequest> productRequests=productRequestRepository.findByStatus(ProductRequestStatus.PENDING,page);
        return productRequests.map(this::convertProductRequestToDto);
    }

    @Override
    public ProductRequestResponse viewProductAddRequest(Long id){
        com.Kee.V2C.entity.ProductRequest productRequest=productRequestRepository.findById(id).
                orElseThrow(()->new ResourceNotFoundException("no product request with id: "+id));
        return convertProductRequestToDto(productRequest);
    }
    @Override
    @Transactional
    public ProductRequestResponse rejectProductAddRequest(Long id){
        com.Kee.V2C.entity.ProductRequest productRequest=productRequestRepository.findById(id).
                orElseThrow(()->new ResourceNotFoundException("no product request with id: "+id));
        productRequest.setStatus(ProductRequestStatus.REJECTED);
        productRequestRepository.save(productRequest);
        return convertProductRequestToDto(productRequest);
    }

    @Override
    @Transactional
    public ProductModelResponse processProductAddRequest(Long requestId, AdminAdditionOnProductRequest adminAdditionOnProductRequest){
        com.Kee.V2C.entity.ProductRequest request=productRequestRepository.findById(requestId).
                orElseThrow(()->new ResourceNotFoundException("request with id: "+requestId+" not found"));

        if(request.getStatus()==ProductRequestStatus.APPROVED || request.getStatus()==ProductRequestStatus.REJECTED) {
            throw new ResourceAlreadyExistsException("request was already processed");
        }

        Vendor vendor=request.getVendor();
        Brand brand=null;
        if(adminAdditionOnProductRequest.brandId()!=null) {
            brand = brandRepository.findById(adminAdditionOnProductRequest.brandId()).orElseThrow(
                    () -> new ResourceNotFoundException("Brand not found")
            );
        }
        SubCategory subCategory=subCategoryRepository.findById(adminAdditionOnProductRequest.subcategoryId())
                .orElseThrow(()->new ResourceNotFoundException("subCategory not found"));

        ProductModel productModel=new ProductModel(adminAdditionOnProductRequest.modifiedName(),
                adminAdditionOnProductRequest.modifiedDescription(),
                (adminAdditionOnProductRequest.modifiedImageFile()==null)?request.getImageUrl():imageService.saveImage(adminAdditionOnProductRequest.modifiedImageFile()
                        , PathFolder.MODELS), vendor,
                adminAdditionOnProductRequest.modifiedGlobal(), ProductModelStatus.ACTIVE,brand,
                subCategory
        );
        subCategory.addProductModel(productModel);
        if(!adminAdditionOnProductRequest.modifiedGlobal()){
            vendor.addProductModel(productModel);
        }
        request.setStatus(ProductRequestStatus.APPROVED);
        productRequestRepository.save(request);
        productModelRepository.save(productModel);

        return productModelService.convertProductModelToDto(productModel);
    }

    private ProductRequestResponse convertProductRequestToDto(com.Kee.V2C.entity.ProductRequest productRequest){
        return new ProductRequestResponse(
                productRequest.getId(), productRequest.getName(), productRequest.getDescription(),
                productRequest.getImageUrl(),productRequest.getGlobal(),productRequest.getStatus()
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
