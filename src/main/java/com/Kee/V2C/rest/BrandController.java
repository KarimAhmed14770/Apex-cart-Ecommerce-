package com.Kee.V2C.rest;

import com.Kee.V2C.dto.brand.BrandRegisterRequest;
import com.Kee.V2C.dto.brand.BrandResponse;
import com.Kee.V2C.dto.brand.BrandUpdateRequest;
import com.Kee.V2C.service.Brand.BrandService;
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
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService){
        this.brandService=brandService;
    }



    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BrandResponse> addBrand(@Valid @ModelAttribute BrandRegisterRequest brandRegisterRequest){
        BrandResponse brandResponse=brandService.addBrand(brandRegisterRequest);

        URI location= ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(brandResponse.id())
                .toUri();
        //returning also the url path to find the created source to make it available for the frontend
        //so they don't have to reconstruct it
        return ResponseEntity.created(location).body(brandResponse);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(value="/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BrandResponse> updateBrand(@PathVariable("id") Long id,@Valid @ModelAttribute BrandUpdateRequest brandUpdateRequest){
        return ResponseEntity.ok(brandService.updateBrand(id, brandUpdateRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("delete/{id}")
    public ResponseEntity<BrandResponse> updateBrand(@PathVariable("id")Long id){
        return ResponseEntity.ok(brandService.softDeleteBrand(id));
    }


    @GetMapping("/")
    public ResponseEntity<Page<BrandResponse>> getBrands(Pageable page){
        return ResponseEntity.status(HttpStatus.OK).body(brandService.getActiveBrands(page).map(brandService::convertBrandToDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable("id") Long id){
        return ResponseEntity.status(HttpStatus.OK).body( brandService.convertBrandToDto(brandService.getBrandById(id)));
    }

}
