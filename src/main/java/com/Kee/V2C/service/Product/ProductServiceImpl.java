package com.Kee.V2C.service.Product;

import com.Kee.V2C.Repository.ProductRepository;
import com.Kee.V2C.Repository.VendorRepository;
import com.Kee.V2C.dto.product.ProductResponse;
import com.Kee.V2C.dto.product.ProductUpdateRequest;
import com.Kee.V2C.dto.product.ProductViewResponse;
import com.Kee.V2C.entity.Product;
import com.Kee.V2C.entity.Vendor;
import com.Kee.V2C.exception.ResourceNotFoundException;
import com.Kee.V2C.mapper.ProductMapper;
import com.Kee.V2C.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final SecurityUtil securityUtil;
    private final VendorRepository vendorRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,ProductMapper productMapper,
                              SecurityUtil securityUtil,VendorRepository vendorRepository){
        this.productRepository=productRepository;
        this.productMapper=productMapper;
        this.securityUtil=securityUtil;
        this.vendorRepository=vendorRepository;
    }


    @Override
    public Page<ProductViewResponse> getProductByDescription(String description, Pageable page){
        Page<Product> products=productRepository.findByDescriptionContainingIgnoreCaseAndActiveTrue(description,page);
        return products.map(this::convertToDto);
    }

    @Override
    public ProductViewResponse getProductById(Long id){
        Product product=productRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException("product with id: "+id+" not found."));
        return convertToDto(product);
    }


    @Override
    public Page<ProductResponse> showMyProducts(Pageable page){
        Vendor vendor=getCurrentVendor();
        Page<Product> products=productRepository.findAllByVendorId(vendor.getId(), page);
        return products.map(this::convertProductToDto);
    }


    @Override
    @Transactional
    public ProductResponse updateProductInfo(Long id, ProductUpdateRequest productUpdateRequest){
        Product product=productRepository.findById(id).orElseThrow(
                ()->new ResourceNotFoundException("no product with id: "+id)
        );
        productMapper.updateProductFromDto(productUpdateRequest,product);
        return convertProductToDto(product);
    }



    private ProductViewResponse convertToDto(Product product){
        ProductViewResponse productViewResponse=new ProductViewResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock().getQuantity(),
                product.getProductModel().getImageUrl(),
                product.getImageUrl(),
                product.getProductModel().getSubCategory().getName()
        );
        return productViewResponse;
    }
    private ProductResponse convertProductToDto(Product product){
        return new ProductResponse(
                product.getId(),
                product.getProductModel().getId(),
                product.getProductModel().getSubCategory().getId(),
                product.getStock().getShop().getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock().getQuantity(),
                product.getProductModel().getImageUrl(),
                product.getActive()

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
