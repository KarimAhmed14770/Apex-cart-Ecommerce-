package com.Kee.V2C.service.Brand;

import com.Kee.V2C.Repository.BrandRepository;
import com.Kee.V2C.dto.brand.BrandRegisterRequest;
import com.Kee.V2C.dto.brand.BrandResponse;
import com.Kee.V2C.dto.brand.BrandUpdateRequest;
import com.Kee.V2C.entity.Brand;
import com.Kee.V2C.enums.PathFolder;
import com.Kee.V2C.exception.ResourceAlreadyExistsException;
import com.Kee.V2C.exception.ResourceNotFoundException;
import com.Kee.V2C.mapper.BrandMapper;
import com.Kee.V2C.service.Image.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandServiceImpl implements BrandService{
    private final BrandRepository brandRepository;
    private final ImageService imageService;
    private final BrandMapper brandMapper;

    @Autowired
    public BrandServiceImpl(BrandRepository brandRepository,ImageService imageService,
                            BrandMapper brandMapper){
        this.brandRepository=brandRepository;
        this.brandMapper=brandMapper;
        this.imageService=imageService;
    }


    @Override
    @Transactional
    public BrandResponse addBrand(BrandRegisterRequest brandRegisterRequest){
        if(brandRepository.existsByNameIgnoreCase(brandRegisterRequest.name())){
            throw new ResourceAlreadyExistsException("Brand already exists");
        }
        Brand brand=new Brand(brandRegisterRequest.name(), brandRegisterRequest.description()
                ,imageService.saveImage(brandRegisterRequest.imageFile(), PathFolder.BRANDS), brandRegisterRequest.active());
        brandRepository.save(brand);
        return convertBrandToDto(brand);
    }


    @Override
    @Transactional
    public BrandResponse updateBrand(Long id, BrandUpdateRequest brandUpdateRequest){
        Brand brand=brandRepository.findById(id).orElseThrow(
                ()->new ResourceNotFoundException("Brand not found")
        );
        brandMapper.updateBrandFromDto(brandUpdateRequest,brand);
        if(brandUpdateRequest.imageFile()!=null && !brandUpdateRequest.imageFile().isEmpty()) {
            String updated_img = imageService.saveImage(brandUpdateRequest.imageFile(), PathFolder.BRANDS);
            brand.setImageUrl(updated_img);
        }
        brandRepository.save(brand);
        return convertBrandToDto(brand);
    }


    @Override
    @Transactional
    public BrandResponse softDeleteBrand(Long id){
        Brand brand=brandRepository.findById(id).orElseThrow(
                ()->new ResourceNotFoundException("Brand not found")
        );
        brand.setActive(false);
        brandRepository.save(brand);
        return convertBrandToDto(brand);
    }

    @Override
    public Brand getBrandById(Long id){
        return brandRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("brand with id: "+id+" not found."));
    }

    @Override
    public Page<Brand> getActiveBrands(Pageable page){
        return brandRepository.findByActiveTrue(page);
    }

    public BrandResponse convertBrandToDto(Brand brand){
        return new BrandResponse(brand.getId(), brand.getName(), brand.getDescription(), brand.getImageUrl(),brand.getActive());
    }
}
