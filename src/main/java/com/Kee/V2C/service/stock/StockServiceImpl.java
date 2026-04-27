package com.Kee.V2C.service.stock;

import com.Kee.V2C.Repository.*;
import com.Kee.V2C.dto.product.ProductAddToStockRequest;
import com.Kee.V2C.dto.product.ProductResponse;
import com.Kee.V2C.entity.Product;
import com.Kee.V2C.entity.ProductModel;
import com.Kee.V2C.entity.Stock;
import com.Kee.V2C.entity.Vendor;
import com.Kee.V2C.enums.PathFolder;
import com.Kee.V2C.exception.ResourceNotFoundException;
import com.Kee.V2C.exception.UserAccessDeniedException;
import com.Kee.V2C.service.Image.ImageService;
import com.Kee.V2C.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockServiceImpl implements StockService{

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final ProductModelRepository productModelRepository;
    private final SecurityUtil securityUtil;
    private final ImageService imageService;
    private final VendorRepository vendorRepository;

    @Autowired
    public StockServiceImpl(ProductRepository productRepository,StockRepository stockRepository,
                            ProductModelRepository productModelRepository,SecurityUtil securityUtil,
                            ImageService imageService,VendorRepository vendorRepository){
        this.productRepository=productRepository;
        this.stockRepository=stockRepository;
        this.productModelRepository=productModelRepository;
        this.securityUtil=securityUtil;
        this.imageService=imageService;
        this.vendorRepository=vendorRepository;
    }

    @Override
    @Transactional
    public ProductResponse addProductToStock(ProductAddToStockRequest productAddToStockRequest){
        return convertProductToDto(addProductFromRequest(productAddToStockRequest));
    }

    @Override
    @Transactional
    public ProductResponse addStock(Long id, Integer quantity){
        Product product=productRepository.findById(id).orElseThrow(
                ()->new ResourceNotFoundException("no product with id: "+id)
        );
        Vendor vendor=getCurrentVendor();
        if(!vendor.getId().equals(product.getVendor().getId())){
            throw new UserAccessDeniedException("You can't modify this stock");
        }
        stockRepository.incrementProductStock(quantity,product.getId(),product.getStock().getShop().getId());
        int current_qty=product.getStock().getQuantity();//manual sync because customized query doesn't sync the db
        //with the pojo
        product.getStock().setQuantity(quantity+current_qty);
        return convertProductToDto(product);
    }

    private ProductResponse convertProductToDto(Product product){
        return new ProductResponse(
                product.getId(),
                product.getProductModel().getId(),
                product.getProductModel().getSubCategory().getId(),
                product.getStock().getShop().getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock().getQuantity(),
                product.getProductModel().getImageUrl(),
                product.getActive()

        );
    }

    private Product addProductFromRequest(ProductAddToStockRequest productAddToStockRequest){
        ProductModel productModel=productModelRepository.findById(productAddToStockRequest.modelId())
                .orElseThrow(()->new ResourceNotFoundException("there is no model for this product"));
        Long vendorId=securityUtil.getCurrentUserId();
        Vendor vendor=vendorRepository.findByIdWithShopWithStock(vendorId).//getting the shop info to prevent n+1
                orElseThrow(()->new ResourceNotFoundException("no vendor with id: "+vendorId));

        if (productModel.isGlobal() || (productModel.getVendor() != null &&
                        productModel.getVendor().getId().equals(vendorId))){
            Product product = new Product(vendor, productModel, productAddToStockRequest.name(), productAddToStockRequest.description(),
                    productAddToStockRequest.price(), imageService.saveImage(productAddToStockRequest.imageFile(), PathFolder.PRODUCTS));
            Stock stock = new Stock(productAddToStockRequest.stock(), product, vendor.getShop());
            stock.setActive(true);
            product.setStock(stock);
            vendor.addProduct(product);
            vendor.getShop().getStocks().add(stock);
            product.setActive(productAddToStockRequest.status());
            productRepository.save(product);

            return product;
        }
        else {
            throw new UserAccessDeniedException("can't add this model");
        }
    }
    private Vendor getCurrentVendor(){
        Long userId=securityUtil.getCurrentUserId();
        Vendor vendor= vendorRepository.findById(userId)
                .orElseThrow(()->new UsernameNotFoundException("Seller with id: "
                        +userId+"does not exist"));
        return vendor;
    }
}
