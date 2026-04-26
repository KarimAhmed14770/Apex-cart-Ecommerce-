package com.Kee.V2C.service.cart;

import com.Kee.V2C.Repository.CartItemRepository;
import com.Kee.V2C.Repository.CustomerRepository;
import com.Kee.V2C.Repository.ProductRepository;
import com.Kee.V2C.dto.cart.CartItemRequest;
import com.Kee.V2C.dto.cart.CartItemResponse;
import com.Kee.V2C.dto.cart.CartResponse;
import com.Kee.V2C.entity.CartItem;
import com.Kee.V2C.entity.Customer;
import com.Kee.V2C.entity.Product;
import com.Kee.V2C.exception.*;
import com.Kee.V2C.utils.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService{
    private final SecurityUtil securityUtil;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(SecurityUtil securityUtil,CartItemRepository cartItemRepository
            ,CustomerRepository customerRepository,ProductRepository productRepository){
        this.cartItemRepository=cartItemRepository;
        this.securityUtil=securityUtil;
        this.customerRepository=customerRepository;
        this.productRepository=productRepository;
    }


    @Override
    @Transactional
    public CartResponse addToCart(CartItemRequest cartItemRequest){
        Customer customer=getCurrentCustomer();
        Product product=getProductById(cartItemRequest.productId());
        int requiredQuantity=0;

        if(cartItemRepository.existsByCustomerIdAndProductId(customer.getId(),product.getId())){
            CartItem cartItem=cartItemRepository.findByCustomerIdAndProductId(customer.getId(),product.getId())
                    .orElseThrow(()->new CartItemNotFoundException("cartItem not found"));
            requiredQuantity=cartItem.getQuantity()+cartItemRequest.quantity();
            cartItemHandling(product,cartItem,requiredQuantity);
        }
        else{
            requiredQuantity=cartItemRequest.quantity();
            CartItem newcartItem=new CartItem(requiredQuantity,customer,product);
            cartItemHandling(product,newcartItem,requiredQuantity);

        }

        return viewMyCart();
    }

    @Override
    public CartResponse viewMyCart(){
        Long id=securityUtil.getCurrentUserId();
        List<CartItem> cartItems=cartItemRepository.findAllByCustomerId(id);
        if(cartItems.isEmpty()){
            throw new CartEmptyException("your shopping cart is currently empty");
        }
        return convertCartListToDto(cartItems);
    }

    private void cartItemHandling(Product product,CartItem cartItem,int requiredQuantity){
        if(requiredQuantity>0){
            //this if condition is hardcoded now for testing only 1 inventory
            //in future updates there will be multiple inventories and the inventory choice will
            //be dependent on user location ,aldo is stock isn't sufficient at one inventory we can
            //use the stock from another inventory
            if(requiredQuantity<=product.getStock().getQuantity()){
                cartItem.setQuantity(requiredQuantity);
                cartItemRepository.save(cartItem);
            }
            else{
                throw new InsufficientStockException("Insufficient stock for product with id: " +
                        product.getId() +" ." +
                        "Requested quantity= "+requiredQuantity+
                        "          Available Stock= "
                        +product.getStock().getQuantity());
            }
        }
        else{
            cartItemRepository.delete(cartItem);
            cartItemRepository.flush(); // Forces the delete to happen immediatly
        }

    }

    private Customer getCurrentCustomer(){
        Long id=securityUtil.getCurrentUserId();
        return customerRepository.findByIdWithCredentials(id).
                orElseThrow(()->new UserNotFoundException("customer not found"));
    }

    private Product getProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow(()->new ProductNotFoundException("product with id: "+id+" not found."));
    }

    private CartItemResponse convertCartItemToDto(CartItem cartItem){
        CartItemResponse cartItemResponse=new CartItemResponse(
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getQuantity(),
                cartItem.getProduct().getPrice(),
                cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
        );
        return cartItemResponse;
    }

    private CartResponse convertCartListToDto(List<CartItem> cartItems){
        BigDecimal totalPrice=new BigDecimal(0);
        List<CartItemResponse> cartItemResponses=new ArrayList<>();
        for(CartItem cartItem:cartItems){
            CartItemResponse cartItemResponse=convertCartItemToDto(cartItem);
            cartItemResponses.add(cartItemResponse);
            totalPrice=totalPrice.add(cartItemResponse.subtotal());
        }
        return new CartResponse(cartItemResponses,totalPrice);
    }

}
