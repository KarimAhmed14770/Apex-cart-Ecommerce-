package com.Kee.V2C.rest;


import com.Kee.V2C.dto.Authentication.StatusUpdateDto;
import com.Kee.V2C.dto.product.*;
import com.Kee.V2C.dto.vendor.*;
import com.Kee.V2C.enums.UserStatus;
import com.Kee.V2C.service.Vendor.VendorServiceImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<VendorProfileResponse>> getAllVendors(Pageable page){
        return ResponseEntity.ok(vendorService.getAllVendors(page));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<VendorProfileResponse> getVendorById(@PathVariable("id") Long id){
        return ResponseEntity.ok(vendorService.getVendorProfileById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<Page<VendorProfileResponse>> getVendorByAttribute(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) Float lowerRating,
            @RequestParam(required = false) Float higherRating,
            Pageable page){
        return ResponseEntity.ok(vendorService.searchForVendor(name,description,address,status
                ,lowerRating,higherRating,page));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("modify-status/{id}")
    public VendorProfileResponse modifyVendorStatus(@PathVariable("id") Long id,
                                                    @RequestBody @Valid StatusUpdateDto status){
        return vendorService.modifyVendorStatus(id,status);
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
