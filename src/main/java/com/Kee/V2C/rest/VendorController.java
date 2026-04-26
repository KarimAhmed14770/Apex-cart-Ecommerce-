package com.Kee.V2C.rest;


import com.Kee.V2C.dto.product.*;
import com.Kee.V2C.dto.vendor.*;
import com.Kee.V2C.service.Vendor.VendorServiceImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorServiceImpl vendorService;

    public VendorController(VendorServiceImpl sellerService){
        this.vendorService=sellerService;
    }

    @PatchMapping(value = "/my-profile/update",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VendorProfileResponse> updateSeller( @Valid @ModelAttribute VendorUpdateProfileRequest vendorUpdateProfileRequest){
        return ResponseEntity.status(HttpStatus.OK).body(vendorService.updateVendorProfile(vendorUpdateProfileRequest));
    }

    @GetMapping("/my-profile")
    public ResponseEntity<VendorProfileResponse> myProfile(){
        return ResponseEntity.status(HttpStatus.OK).body(vendorService.myProfile());
    }

}
