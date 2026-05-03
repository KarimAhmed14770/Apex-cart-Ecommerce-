package com.Kee.V2C.service.Vendor;

import com.Kee.V2C.Repository.*;
import com.Kee.V2C.dto.Authentication.StatusUpdateDto;
import com.Kee.V2C.dto.product.*;
import com.Kee.V2C.dto.vendor.*;
import com.Kee.V2C.entity.*;
import com.Kee.V2C.enums.PathFolder;
import com.Kee.V2C.enums.ProductModelStatus;
import com.Kee.V2C.enums.ProductRequestStatus;
import com.Kee.V2C.enums.UserStatus;
import com.Kee.V2C.exception.ResourceAlreadyExistsException;
import com.Kee.V2C.exception.ResourceNotFoundException;
import com.Kee.V2C.exception.UserNotFoundException;
import com.Kee.V2C.mapper.ProductMapper;
import com.Kee.V2C.mapper.ShopMapper;
import com.Kee.V2C.mapper.VendorMapper;
import com.Kee.V2C.service.Image.ImageService;
import com.Kee.V2C.service.Vendor.VendorService;
import com.Kee.V2C.specifications.ProductModelSpecs;
import com.Kee.V2C.specifications.VendorSpecs;
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
                                                       String address, UserStatus status, Float lowerRating,
                                                       Float higherRating, Pageable page){
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
    public VendorProfileResponse  modifyVendorStatus(Long id, StatusUpdateDto status){
        Vendor vendor=getVendorById(id);
        vendor.getCredential().setUserStatus(status.status());
        vendorRepository.save(vendor);

        return convertVendorToDto(vendor);
    }





    private Vendor getCurrentVendor(){
        Long userId=securityUtil.getCurrentUserId();
        Vendor vendor= vendorRepository.findById(userId)
                .orElseThrow(()->new UsernameNotFoundException("Seller with id: "
                        +userId+"does not exist"));
        return vendor;
    }

    private Vendor getVendorById(Long id){
        Vendor vendor=vendorRepository.findByIdWithCredentials(id).orElseThrow(
                ()->new ResourceNotFoundException("vendor with id: "+id+" not found.")
        );
        return vendor;
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
}