package com.Kee.V2C.service.Brand;

import com.Kee.V2C.dto.brand.BrandRegisterRequest;
import com.Kee.V2C.dto.brand.BrandResponse;
import com.Kee.V2C.dto.brand.BrandUpdateRequest;
import com.Kee.V2C.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BrandService {
    BrandResponse addBrand(BrandRegisterRequest brandRegisterRequest);
    BrandResponse updateBrand(Long id, BrandUpdateRequest brandUpdateRequest);
    BrandResponse softDeleteBrand(Long id);
    Brand getBrandById(Long id);
    Page<Brand> getActiveBrands(Pageable page);
    BrandResponse convertBrandToDto(Brand brand);

}
