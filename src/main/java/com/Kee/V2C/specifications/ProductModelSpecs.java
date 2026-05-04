package com.Kee.V2C.specifications;

import com.Kee.V2C.entity.Brand;
import com.Kee.V2C.entity.ProductModel;
import com.Kee.V2C.entity.SubCategory;
import com.Kee.V2C.entity.Vendor;
import com.Kee.V2C.enums.ProductModelStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class ProductModelSpecs {
    private ProductModelSpecs(){}

    public static Specification<ProductModel> hasName(String name){
        return (root,query,cb)->{
          if(name==null ||name.isEmpty())return null;
          return cb.like(cb.lower(root.get("name")),"%"+name+"%");
        };
    }
    public static Specification<ProductModel> hasDescription(String description){
        return (root,query,cb)->{
            if(description==null ||description.isEmpty())return null;
            return cb.like(cb.lower(root.get("description")),"%"+description+"%");
        };
    }
    public static Specification<ProductModel> isGlobal(Boolean isGlobal){
        return (root,query,cb)->{
            if(isGlobal==null)return null;
            return cb.equal(root.get("isGlobal"),isGlobal);
        };
    }

    public static Specification<ProductModel> hasStatus(ProductModelStatus status){
        return (root,query,cb)->{
            if(status==null)return null;
            return cb.equal(root.get("status"),status);
        };
    }

    public static Specification<ProductModel> hasBrand(Long brandId){
        return (root,query,cb)->{
            if(brandId==null)return null;
            return cb.equal(root.get("brand").get("id"),brandId);
        };
    }

    public static Specification<ProductModel> hasSubCategory(Long subCategoryId){
        return (root,query,cb)->{
            if(subCategoryId==null)return null;
            return cb.equal(root.get("subCategory").get("id"),subCategoryId);
        };
    }

    public static Specification<ProductModel> hasVendor(Long vendorId){
        return (root,query,cb)->{
            if(vendorId==null)return null;
            return cb.equal(root.get("owner").get("id"),vendorId);
        };
    }

}
