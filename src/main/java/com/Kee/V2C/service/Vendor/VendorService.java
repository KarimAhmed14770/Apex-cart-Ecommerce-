package com.Kee.V2C.service.Vendor;

import com.Kee.V2C.dto.product.*;
import com.Kee.V2C.dto.vendor.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

interface VendorService {
    VendorProfileResponse updateVendorProfile(VendorUpdateProfileRequest vendorUpdateProfileRequest);
    VendorProfileResponse myProfile();


}
