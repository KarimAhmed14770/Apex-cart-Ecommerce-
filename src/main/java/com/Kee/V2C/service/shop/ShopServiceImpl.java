package com.Kee.V2C.service.shop;

import com.Kee.V2C.Repository.ProductRepository;
import com.Kee.V2C.Repository.ShopRepository;
import com.Kee.V2C.Repository.VendorRepository;
import com.Kee.V2C.dto.product.ProductResponse;
import com.Kee.V2C.dto.vendor.ShopRegisterRequest;
import com.Kee.V2C.dto.vendor.ShopResponse;
import com.Kee.V2C.dto.vendor.ShopUpdateRequest;
import com.Kee.V2C.dto.vendor.ShopViewResponse;
import com.Kee.V2C.entity.Product;
import com.Kee.V2C.entity.Shop;
import com.Kee.V2C.entity.Vendor;
import com.Kee.V2C.exception.ResourceAlreadyExistsException;
import com.Kee.V2C.exception.ResourceNotFoundException;
import com.Kee.V2C.mapper.ShopMapper;
import com.Kee.V2C.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopServiceImpl implements ShopService{

    private final ShopRepository shopRepository;
    private final ShopMapper shopMapper;
    private final SecurityUtil securityUtil;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ShopServiceImpl(ShopRepository shopRepository,ShopMapper shopMapper,SecurityUtil securityUtil,
                           VendorRepository vendorRepository,ProductRepository productRepository){
        this.shopRepository=shopRepository;
        this.shopMapper=shopMapper;
        this.vendorRepository=vendorRepository;
        this.securityUtil=securityUtil;
        this.productRepository=productRepository;
    }


    @Override
    @Transactional
    public ShopResponse registerShop(ShopRegisterRequest shopRegisterRequest){
        Vendor vendor=getCurrentVendor();
        if(shopRepository.existsByVendorId(vendor.getId())){
            throw new ResourceAlreadyExistsException("you already have a shop");
        }
        Shop shop=new Shop(shopRegisterRequest.name(), shopRegisterRequest.address(), shopRegisterRequest.active(),vendor);
        vendor.setShop(shop);
        shopRepository.save(shop);

        return convertShopToDto(shop);
    }

    @Override
    @Transactional
    public ShopResponse updateShopInfo(ShopUpdateRequest shopRequest){
        Long id=getCurrentVendor().getId();
        Shop shop=shopRepository.findByVendorId(id).orElseThrow(
                ()->new ResourceNotFoundException("vendor with id: "+id+" didn't register a shop yet.")
        );
        shopMapper.updateShopFromDto(shopRequest,shop);
        shopRepository.save(shop);
        return convertShopToDto(shop);
    }

    @Override
    public ShopViewResponse viewShop(Pageable page){
        Vendor vendor=getCurrentVendor();
        Shop shop=shopRepository.findByVendorId(vendor.getId())
                .orElseThrow(()->new ResourceNotFoundException("there are no shops for vendor with id: "+vendor.getId()+"."));

        return viewShop(vendor,shop,page);
    }
    @Override
    @Transactional
    public ShopResponse deactivateShop(){
        Long id=getCurrentVendor().getId();
        Shop shop=shopRepository.findByVendorId(id).orElseThrow(
                ()->new ResourceNotFoundException("vendor with id: "+id+" didn't register a shop yet.")
        );
        shop.setActive(false);
        shopRepository.save(shop);
        return convertShopToDto(shop);
    }

    @Override
    @Transactional
    public ShopResponse activateShop(){
        Long id=getCurrentVendor().getId();
        Shop shop=shopRepository.findByVendorId(id).orElseThrow(
                ()->new ResourceNotFoundException("vendor with id: "+id+" didn't register a shop yet.")
        );
        shop.setActive(true);
        shopRepository.save(shop);
        return convertShopToDto(shop);
    }

    private ShopResponse convertShopToDto(Shop shop){
        return new ShopResponse(shop.getId(), shop.getName(), shop.getAddress(), shop.isActive());
    }

    private Vendor getCurrentVendor(){
        Long userId=securityUtil.getCurrentUserId();
        Vendor vendor= vendorRepository.findById(userId)
                .orElseThrow(()->new UsernameNotFoundException("Seller with id: "
                        +userId+"does not exist"));
        return vendor;
    }
    private ShopViewResponse viewShop(Vendor vendor,Shop shop,Pageable page){
        Page<Product> products=productRepository.findProductByStockShopId(shop.getId(),page);

        return new ShopViewResponse(vendor.getImageUrl(),
                convertShopToDto(shop),
                products.map(this::convertProductToDto));
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
}
