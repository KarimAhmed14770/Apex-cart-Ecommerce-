package com.Kee.V2C.service.Vendor;

import com.Kee.V2C.Repository.*;
import com.Kee.V2C.dto.product.*;
import com.Kee.V2C.dto.vendor.*;
import com.Kee.V2C.entity.*;
import com.Kee.V2C.enums.PathFolder;
import com.Kee.V2C.enums.ProductModelStatus;
import com.Kee.V2C.enums.ProductRequestStatus;
import com.Kee.V2C.exception.ResourceAlreadyExistsException;
import com.Kee.V2C.exception.ResourceNotFoundException;
import com.Kee.V2C.mapper.ProductMapper;
import com.Kee.V2C.mapper.ShopMapper;
import com.Kee.V2C.mapper.VendorMapper;
import com.Kee.V2C.service.Image.ImageService;
import com.Kee.V2C.service.Vendor.VendorService;
import com.Kee.V2C.specifications.ProductModelSpecs;
import com.Kee.V2C.utils.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorServiceImpl implements VendorService {
    private final SecurityUtil securityUtil;
    private final VendorRepository vendorRepository;
    private final VendorMapper vendorMapper;

    public VendorServiceImpl(SecurityUtil securityUtil, VendorRepository vendorRepository,
                            VendorMapper vendorMapper){
        this.securityUtil=securityUtil;
        this.vendorRepository = vendorRepository;
        this.vendorMapper=vendorMapper;
    }

    @Transactional
    public VendorProfileResponse updateVendorProfile(VendorUpdateProfileRequest vendorUpdateProfileRequest){
        Vendor vendor=getCurrentVendor();
        vendorMapper.updateVendorFromDto(vendorUpdateProfileRequest,vendor);
        vendorRepository.save(vendor);
        return new VendorProfileResponse(
                vendor.getId(),
                vendor.getName(),
                vendor.getAddress(),
                vendor.getImageUrl(),
                vendor.getRating(),
                vendor.getCredential().getUserStatus().name()
                );
    }

    public VendorProfileResponse myProfile(){
        Vendor vendor=getCurrentVendor();
        return new VendorProfileResponse(
                vendor.getId(),
                vendor.getName(),
                vendor.getAddress(),
                vendor.getImageUrl(),
                vendor.getRating(),
                vendor.getCredential().getUserStatus().name());
    }




    private Vendor getCurrentVendor(){
        Long userId=securityUtil.getCurrentUserId();
        Vendor vendor= vendorRepository.findById(userId)
                .orElseThrow(()->new UsernameNotFoundException("Seller with id: "
                        +userId+"does not exist"));
        return vendor;
    }

}