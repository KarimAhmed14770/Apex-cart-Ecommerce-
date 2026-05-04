package com.Kee.V2C.specifications;

import com.Kee.V2C.entity.Product;
import com.Kee.V2C.entity.Vendor;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecs {
    private ProductSpecs(){}

    public static Specification<Product> hasName(String name){
        return (root,query,cb)->{
            if(name==null ||name.isEmpty())return null;
            return cb.like(cb.lower(root.get("name")),"%"+name.toLowerCase()+"%");
        };
    }
    public static Specification<Product> hasDescription(String description){
        return (root,query,cb)->{
            if(description==null ||description.isEmpty())return null;
            return cb.like(cb.lower(root.get("description")),"%"+description.toLowerCase()+"%");
        };
    }


    public static Specification<Product> isActive(Boolean active){
        return (root,query,cb)->{
            if(active==null)return null;
            return cb.equal(root.get("active"),active);
        };
    }

    public static Specification<Product> hasBrand(Long brandId){
        return (root,query,cb)->{
            if(brandId==null)return null;
            return cb.equal(root.get("productModel").get("brand").get("id"),brandId);
        };
    }
    public static Specification<Product> hasParentCategory(Long parentCategoryId){
        return (root,query,cb)->{
            if(parentCategoryId==null)return null;
            return cb.equal(root.get("productModel").get("subCategory").get("parentCategory").get("id"),parentCategoryId);
        };
    }
    public static Specification<Product> hasSubCategory(Long subCategoryId){
        return (root,query,cb)->{
            if(subCategoryId==null)return null;
            return cb.equal(root.get("productModel").get("subCategory").get("id"),subCategoryId);
        };
    }

    public static Specification<Product> hasVendor(Long vendorId){
        return (root,query,cb)->{
            if(vendorId==null)return null;
            return cb.equal(root.get("vendor").get("id"),vendorId);
        };
    }
    public static Specification<Product> hasPriceBetween(Float lowerRange, Float higherRange){
        return (root, query, criteriaBuilder) -> {
            if(lowerRange==null && higherRange==null) return null;
            if(lowerRange!=null &&higherRange==null){
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"),lowerRange);
            }
            if(lowerRange==null &&higherRange!=null){
                return criteriaBuilder.lessThanOrEqualTo(root.get("price"),higherRange);
            }
            return criteriaBuilder.between(root.get("price"),lowerRange,higherRange);
        };
    }

}
