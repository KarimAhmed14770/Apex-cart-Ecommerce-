package com.Kee.V2C.service.Vendor;

import com.Kee.V2C.dto.Authentication.StatusUpdateDto;
import com.Kee.V2C.dto.product.*;
import com.Kee.V2C.dto.vendor.*;
import com.Kee.V2C.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

interface VendorService {
    VendorProfileResponse getVendorProfileById(Long id);
    public Page<VendorProfileResponse> searchForVendor(String name, String description,
                                                       String address, UserStatus status, Float lowerRating,
                                                       Float higherRating, Pageable pageable);

    Page<VendorProfileResponse> getAllVendors( Pageable page);

    VendorProfileResponse  modifyVendorStatus(Long id, StatusUpdateDto status);

    VendorProfileResponse updateVendorProfile(VendorUpdateProfileRequest vendorUpdateProfileRequest);
    VendorProfileResponse myProfile();


}
