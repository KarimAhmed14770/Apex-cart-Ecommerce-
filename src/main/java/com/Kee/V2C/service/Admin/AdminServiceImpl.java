package com.Kee.V2C.service.Admin;

import com.Kee.V2C.Repository.*;
import com.Kee.V2C.dto.Authentication.StatusUpdateDto;
import com.Kee.V2C.dto.brand.BrandRegisterRequest;
import com.Kee.V2C.dto.brand.BrandResponse;
import com.Kee.V2C.dto.brand.BrandUpdateRequest;
import com.Kee.V2C.dto.category.*;
import com.Kee.V2C.dto.customer.CustomerProfileResponse;
import com.Kee.V2C.dto.product.*;
import com.Kee.V2C.dto.vendor.VendorProfileResponse;
import com.Kee.V2C.entity.*;
import com.Kee.V2C.enums.PathFolder;
import com.Kee.V2C.enums.ProductModelStatus;
import com.Kee.V2C.enums.ProductRequestStatus;
import com.Kee.V2C.enums.UserStatus;
import com.Kee.V2C.exception.ResourceAlreadyExistsException;
import com.Kee.V2C.exception.ResourceNotFoundException;
import com.Kee.V2C.exception.UserNotFoundException;
import com.Kee.V2C.mapper.BrandMapper;
import com.Kee.V2C.mapper.CategoryMapper;
import com.Kee.V2C.mapper.ProductModelMapper;
import com.Kee.V2C.mapper.SubCategoryMapper;
import com.Kee.V2C.service.Brand.BrandService;
import com.Kee.V2C.service.Category.CategoryService;
import com.Kee.V2C.service.Image.ImageService;
import com.Kee.V2C.service.Category.SubCategoryService;
import com.Kee.V2C.service.ProductModel.ProductModelService;
import com.Kee.V2C.specifications.CustomerSpecs;
import com.Kee.V2C.specifications.VendorSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {
    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CategoryService categoryService;
    private final SubCategoryService subCategoryService;
    private final SubCategoryRepository subCategoryRepository;
    private final SubCategoryMapper subCategoryMapper;
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final ProductModelRepository productModelRepository;
    private final ProductModelMapper productModelMapper;
    private final ProductRequestRepository productRequestRepository;
    private final ImageService imageService;
    private final BrandService brandService;
    private final ProductModelService productModelService;



    @Autowired
    public AdminServiceImpl(CustomerRepository customerRepository, VendorRepository vendorRepository,
                            CategoryRepository categoryRepository,CategoryMapper categoryMapper,
                            CategoryService categoryService,SubCategoryService subCategoryService,
                            SubCategoryRepository subCategoryRepository,BrandService brandService
                            ,SubCategoryMapper subCategoryMapper,BrandRepository brandRepository,
                            BrandMapper brandMapper,ProductModelRepository productModelRepository,
                            ProductModelMapper productModelMapper,ProductRequestRepository productRequestRepository,
                            ImageService imageService,ProductModelService productModelService){
        this.customerRepository = customerRepository;
        this.vendorRepository = vendorRepository;
        this.categoryRepository=categoryRepository;
        this.categoryMapper=categoryMapper;
        this.categoryService=categoryService;
        this.subCategoryService=subCategoryService;
        this.subCategoryRepository=subCategoryRepository;
        this.subCategoryMapper=subCategoryMapper;
        this.brandRepository=brandRepository;
        this.brandMapper=brandMapper;
        this.productModelRepository=productModelRepository;
        this.productModelMapper=productModelMapper;
        this.productRequestRepository=productRequestRepository;
        this.imageService=imageService;
        this.brandService=brandService;
        this.productModelService=productModelService;
    }

    @Override
    public Page<CustomerProfileResponse> getAllCustomers(Pageable page){
        Specification<Customer> spec=Specification.where(CustomerSpecs.fetchCredential());
        return customerRepository.findAll(spec,page).map(this::convertCustomerToDto);

    }

    @Override
    public CustomerProfileResponse getCustomerProfileById(Long id){
        Customer customer= customerRepository.findByIdWithCredentials(id)
                .orElseThrow(()->new UserNotFoundException("user with id: "+ id +" not  found"));
        return convertCustomerToDto(customer);
    }


    @Override
    public Page<CustomerProfileResponse> searchForCustomer(String userName, String email, String firstName,
                                                           String lastName, String shippingAddress,
                                                           UserStatus status,Pageable pageable){
        // 1. Start with an empty "where true" (conjunction) to avoid null issues
        Specification<Customer> spec = (root, query, cb) -> cb.conjunction();        if(userName!=null) spec=spec.and(CustomerSpecs.hasUserName(userName));

        if(email!=null) spec=spec.and(CustomerSpecs.hasEmail(email));

        if(firstName!=null) spec=spec.and(CustomerSpecs.hasFirstName(firstName));

        if(lastName!=null) spec=spec.and(CustomerSpecs.hasLastName(lastName));

        if(shippingAddress!=null) spec=spec.and(CustomerSpecs.hasAddress(shippingAddress));

        if(status!=null) spec=spec.and(CustomerSpecs.hasStatus(status));
        spec=spec.and(CustomerSpecs.fetchCredential());

        Page<Customer> customers=customerRepository.findAll(spec,pageable);

        return customers.map(this::convertCustomerToDto);
    }


    @Override
    public Page<VendorProfileResponse> getAllVendors(Pageable page){
        Page<Vendor> vendors= vendorRepository.findAll(page);
        return vendors.map(this::convertVendorToDto);
    }

    @Override
    public VendorProfileResponse getVendorProfileById(Long id){
        Vendor vendor=vendorRepository.findByIdWithCredentials(id)
                .orElseThrow(()->new UserNotFoundException("user with id: "+ id +" not  found"));
        return convertVendorToDto(vendor);
    }

    @Override
    public Page<VendorProfileResponse> searchForVendor(String name, String description,
                                                String address, UserStatus status,Float lowerRating,
                                                       Float higherRating,Pageable page){
        Specification<Vendor> spec=(root,query,cb)->cb.conjunction();
        if(name!=null)spec=spec.and(VendorSpecs.hasName(name));
        if(description!=null)spec=spec.and(VendorSpecs.hasDescription(description));
        if(address!=null)spec=spec.and(VendorSpecs.hasAddress(address));
        if(status!=null)spec=spec.and(VendorSpecs.hasStatus(status));
        if(lowerRating!=null || higherRating!=null)spec=spec.and(VendorSpecs.hasRatingBetween(lowerRating,higherRating));
        spec=spec.and(VendorSpecs.fetchCredentials());

        Page<Vendor> vendors=vendorRepository.findAll(spec,page);
        return vendors.map(this::convertVendorToDto);
    }

    @Override
    @Transactional
    public CustomerProfileResponse  modifyCustomerStatus(Long id, StatusUpdateDto status){
        Customer customer=getCustomerById(id);

        customer.getCredential().setUserStatus(status.status());
        customerRepository.save(customer);

        return convertCustomerToDto(customer);

    }

    @Override
    @Transactional
    public VendorProfileResponse  modifyVendorStatus(Long id,StatusUpdateDto status){
        Vendor vendor=getVendorById(id);
        vendor.getCredential().setUserStatus(status.status());
        vendorRepository.save(vendor);

        return convertVendorToDto(vendor);
    }



    @Override
    @Transactional
    public CategoryResponse addCategory(CategoryRegisterRequest categoryRegisterRequest) {
        if(categoryRepository.existsByNameIgnoreCase(categoryRegisterRequest.name())){
            throw new ResourceAlreadyExistsException("category Already exists ");
        }
        Category category=new Category(categoryRegisterRequest.name(), categoryRegisterRequest.description(),
                imageService.saveImage(categoryRegisterRequest.imageFile(),PathFolder.CATEGORIES),
                categoryRegisterRequest.active());
        categoryRepository.save(category);

        return categoryService.convertCategoryToDto(category);
    }



    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest categoryRequest){
        Category updatedCategory=categoryService.getCategoryById(id);
        categoryMapper.updateCategoryFromDto(categoryRequest,updatedCategory);
        if(categoryRequest.imageFile()!=null && !categoryRequest.imageFile().isEmpty()) {
            String updated_img = imageService.saveImage(categoryRequest.imageFile(), PathFolder.CATEGORIES);
            updatedCategory.setImageUrl(updated_img);
        }
        categoryRepository.save(updatedCategory);

        return categoryService.convertCategoryToDto(updatedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse softDeleteCategory(Long id){
        Category category=categoryService.getCategoryById(id);
        category.setActive(false);
        categoryRepository.save(category);
        return categoryService.convertCategoryToDto(category);
    }




    @Override
    @Transactional
    public SubCategoryResponse addSubCategory(Long parentId, SubCategoryRegisterRequest subCategoryRegisterRequest){
        if(subCategoryRepository.existsByNameIgnoreCase(subCategoryRegisterRequest.name())){
            throw new ResourceAlreadyExistsException("category Already exists ");
        }

        Category parentCategory=categoryService.getCategoryById(parentId);
        SubCategory subCategory=new SubCategory(
                parentCategory,
                subCategoryRegisterRequest.name(),
                subCategoryRegisterRequest.description(),
                imageService.saveImage(subCategoryRegisterRequest.imageFile(),PathFolder.SUBCATEGORIES),
                subCategoryRegisterRequest.active());
        parentCategory.addSubcategory(subCategory);//linking parent to sub
        subCategoryRepository.save(subCategory);
        return subCategoryService.convertSubCategoryToDto(subCategory);
    }


    @Override
    @Transactional
    public SubCategoryResponse updateSubCategory(Long id, SubCategoryUpdateRequest subCategoryRequest){
        SubCategory updatedCategory=subCategoryService.getSubCategoryById(id);
        subCategoryMapper.updateSubCategoryFromDto(subCategoryRequest,updatedCategory);
        if(subCategoryRequest.imageFile()!=null && !subCategoryRequest.imageFile().isEmpty()) {
            String updated_img = imageService.saveImage(subCategoryRequest.imageFile(), PathFolder.SUBCATEGORIES);
            updatedCategory.setImageUrl(updated_img);
        }
        subCategoryRepository.save(updatedCategory);

        return subCategoryService.convertSubCategoryToDto(updatedCategory);
    }


    @Override
    @Transactional
    public SubCategoryResponse softDeleteSubCategory(Long id){
        SubCategory updatedCategory=subCategoryService.getSubCategoryById(id);
        updatedCategory.setActive(false);
        subCategoryRepository.save(updatedCategory);
        return subCategoryService.convertSubCategoryToDto(updatedCategory);
    }

    @Override
    @Transactional
    public BrandResponse addBrand(BrandRegisterRequest brandRegisterRequest){
        if(brandRepository.existsByNameIgnoreCase(brandRegisterRequest.name())){
            throw new ResourceAlreadyExistsException("Brand already exists");
        }
        Brand brand=new Brand(brandRegisterRequest.name(), brandRegisterRequest.description()
                ,imageService.saveImage(brandRegisterRequest.imageFile(),PathFolder.BRANDS), brandRegisterRequest.active());
        brandRepository.save(brand);
        return brandService.convertBrandToDto(brand);
    }


    @Override
    @Transactional
    public BrandResponse updateBrand(Long id, BrandUpdateRequest brandUpdateRequest){
        Brand brand=brandRepository.findById(id).orElseThrow(
                ()->new ResourceNotFoundException("Brand not found")
        );
        brandMapper.updateBrandFromDto(brandUpdateRequest,brand);
        if(brandUpdateRequest.imageFile()!=null && !brandUpdateRequest.imageFile().isEmpty()) {
            String updated_img = imageService.saveImage(brandUpdateRequest.imageFile(), PathFolder.BRANDS);
            brand.setImageUrl(updated_img);
        }
        brandRepository.save(brand);
        return brandService.convertBrandToDto(brand);
    }


    @Override
    @Transactional
    public BrandResponse softDeleteBrand(Long id){
        Brand brand=brandRepository.findById(id).orElseThrow(
                ()->new ResourceNotFoundException("Brand not found")
        );
        brand.setActive(false);
        brandRepository.save(brand);
        return brandService.convertBrandToDto(brand);
    }

    @Override
    @Transactional
    public ProductModelResponse addProductModel(ProductModelRegisterRequest productModelRegisterRequest){
        ProductModel productModel=convertProductModelRequestToProductModel(productModelRegisterRequest);
        productModelRepository.save(productModel);
        return productModelService.convertProductModelToDto(productModel);
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

        return productModelService.convertProductModelToDto(productModel);
    }

    @Override
    @Transactional
    public ProductModelResponse softDeleteProductModel(Long id){
        ProductModel productModel=productModelRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Product Model with id: "+id+" not found"));

       productModel.setStatus(ProductModelStatus.DISABLED);
        productModelRepository.save(productModel);

        return productModelService.convertProductModelToDto(productModel);

    }

    public Page<ProductModelResponse> searchProductModel(String name, String description, Long ownerId,
                                                         Long subCategoryId, Long brandId, Boolean isGlobal,
                                                         ProductModelStatus status, Pageable page) {
    Page<ProductModel> productModels=productModelService.getProductModelsByAttributes(name, description, ownerId,
            subCategoryId, brandId, isGlobal, status, page);
    return productModels.map(productModelService::convertProductModelToDto);
    }

    @Override
    @Transactional
    public Page<ProductRequestResponse> getAllProductsRequests(Pageable page){
        Page<ProductRequest> productRequests=productRequestRepository.findAll(page);
        return productRequests.map(this::convertProductRequestToDto);
    }
    @Override
    public Page<ProductRequestResponse> getPendingVendorsProductsRequests(Pageable page){
        Page<ProductRequest> productRequests=productRequestRepository.findByStatus(ProductRequestStatus.PENDING,page);
        return productRequests.map(this::convertProductRequestToDto);
    }

    @Override
    public ProductRequestResponse viewProductAddRequest(Long id){
        ProductRequest productRequest=productRequestRepository.findById(id).
                orElseThrow(()->new ResourceNotFoundException("no product request with id: "+id));
        return convertProductRequestToDto(productRequest);
    }
    @Override
    @Transactional
    public ProductRequestResponse rejectProductAddRequest(Long id){
        ProductRequest productRequest=productRequestRepository.findById(id).
                orElseThrow(()->new ResourceNotFoundException("no product request with id: "+id));
        productRequest.setStatus(ProductRequestStatus.REJECTED);
        productRequestRepository.save(productRequest);
        return convertProductRequestToDto(productRequest);
    }

    @Override
    @Transactional
    public ProductModelResponse processProductAddRequest(Long requestId, AdminAdditionOnProductRequest adminAdditionOnProductRequest){
        ProductRequest request=productRequestRepository.findById(requestId).
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
                            ,PathFolder.MODELS), vendor,
                    adminAdditionOnProductRequest.modifiedGlobal(),ProductModelStatus.ACTIVE,brand,
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



    /*Helper methods*/

    private CustomerProfileResponse convertCustomerToDto(Customer customer) {
        CustomerProfileResponse dto = new CustomerProfileResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhoneNumber(),
                customer.getImageUrl(),
                customer.getShippingAddress(),
                customer.getCredential().getUserStatus().name()
        );
        return dto;
    }
    private VendorProfileResponse convertVendorToDto(Vendor vendor) {
        VendorProfileResponse dto = new VendorProfileResponse(
                vendor.getId(),
              vendor.getName(),
                vendor.getAddress(),
                vendor.getImageUrl(),
                vendor.getRating(),
                vendor.getCredential().getUserStatus().name()

        );
        return dto;
    }

    private Vendor getVendorById(Long id){
        Vendor vendor=vendorRepository.findByIdWithCredentials(id).orElseThrow(
                ()->new ResourceNotFoundException("vendor with id: "+id+" not found.")
        );
        return vendor;
    }

    private Customer getCustomerById(Long id){
        Customer customer=customerRepository.findByIdWithCredentials(id).orElseThrow(
                ()->new ResourceNotFoundException("customer with id: "+id+" not found.")
        );
        return customer;
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



    private ProductRequestResponse convertProductRequestToDto(ProductRequest productRequest){
        return new ProductRequestResponse(
                productRequest.getId(), productRequest.getName(), productRequest.getDescription(),
                productRequest.getImageUrl(),productRequest.getGlobal(),productRequest.getStatus()
        );
    }
}
