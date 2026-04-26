package com.Kee.V2C.service.Customer;

import com.Kee.V2C.Repository.*;
import com.Kee.V2C.dto.cart.CartItemRequest;
import com.Kee.V2C.dto.cart.CartItemResponse;
import com.Kee.V2C.dto.cart.CartResponse;
import com.Kee.V2C.dto.customer.*;
import com.Kee.V2C.dto.order.InvoiceResponse;
import com.Kee.V2C.dto.order.OrderItemResponse;
import com.Kee.V2C.entity.*;
import com.Kee.V2C.enums.UserRoles;
import com.Kee.V2C.enums.UserStatus;
import com.Kee.V2C.exception.*;
import com.Kee.V2C.mapper.CustomerMapper;
import com.Kee.V2C.service.Authentication.JwtService;
import com.Kee.V2C.service.OrderService;
import com.Kee.V2C.utils.SecurityUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SecurityUtil securityUtil;
    private final CartItemRepository cartItemRepository;
    private final StockRepository stockRepository;
    private final OrderRepository orderRepository;
    private final CustomerMapper customerMapper;
    private final OrderService orderService;


    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, PasswordEncoder passwordEncoder,
                               AuthenticationManager authenticationManager, JwtService jwtService,
                               ProductRepository productRepository, SecurityUtil securityUtil
                            , CartItemRepository cartItemRepository, StockRepository stockRepository,
                               OrderRepository orderRepository, CustomerMapper customerMapper,
                               CredentialRepository credentialRepository,OrderService orderService){
        this.customerRepository = customerRepository;
        this.securityUtil=securityUtil;
        this.productRepository=productRepository;
        this.cartItemRepository=cartItemRepository;
        this.stockRepository=stockRepository;
        this.orderRepository=orderRepository;
        this.customerMapper=customerMapper;
        this.orderService=orderService;
    }


    @Override
    public CustomerProfileResponse myProfile(){
        Customer customer=getCurrentCustomer();
        return new CustomerProfileResponse(customer.getId(), customer.getFirstName(),customer.getLastName(),customer.getPhoneNumber()
                ,customer.getImageUrl(),customer.getShippingAddress(),customer.getCredential().getUserStatus().name());
    }

    @Override
    @Transactional
    public CustomerProfileResponse partialUpdateCustomerProfile(CustomerUpdateProfileRequest updateRequest){
        Customer customer=getCurrentCustomer();
        customerMapper.updateCustomerFromDto(updateRequest,customer);
        customerRepository.save(customer);
        return new CustomerProfileResponse(customer.getId(),
                customer.getFirstName(),customer.getLastName(),customer.getPhoneNumber(),customer.getImageUrl(),
                customer.getShippingAddress(),customer.getCredential().getUserStatus().name()
        );
    }


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

    @Override
    @Transactional //to roll back if anything occurs
    public CheckoutResponse checkOut(CheckOutRequest checkOutRequest){
        //Retrieve: Fetch the Cart from the database using the userId.
        List<CartItem> cart=getCustomerCart();
        //Validate: Check if every item in that cart is still in stock (The Atomic Shield).
        cartStockValidationAndUpdate(cart);
        //Convert: Transform the Cart items into Order items and Order
        Order order=orderService.convertCartToOrder(checkOutRequest,cart);
        orderRepository.save(order);

        //empty the cart of the user on the db , this is better than deleting 1 by 1 in loop
        cartItemRepository.deleteAllInBatch(cart);

        //Respond: Return an OrderResponse.
        return new CheckoutResponse(order.getId(),order.getTotalPrice(),order.getStatus().name(),
                order.getOrderedAt(),order.getShippingAddress());
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse generateInvoice(long orderId){
        Customer customer=getCurrentCustomer();
        Order order=orderRepository.findByIdWithSubOrders(orderId).orElseThrow(
                ()->new OrderNotFoundException("you don't have an order with id: "+orderId)
        );
        if(!(order.getCustomer().equals(customer))){
            throw new UserAccessDeniedException ("you can't access this order");
        }
        List<SubOrder> subOrders=new ArrayList<>();
        subOrders=order.getSubOrders();
        List<List<OrderItem>> orderItemsLists=subOrders.stream().map(SubOrder::getOrderItems).toList();
        List<OrderItemResponse> orderItemsResponse=new ArrayList<>();
        for(List<OrderItem> orderItemList:orderItemsLists){
            for(OrderItem orderItem:orderItemList){
            OrderItemResponse response=new OrderItemResponse(
                    orderItem.getProduct().getId(),
                    orderItem.getProduct().getName(),
                    orderItem.getProduct().getProductModel().getImageUrl(),
                    orderItem.getQuantity(),
                    orderItem.getPriceAtPurchase(),
                    orderItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
                    orderItemsResponse.add(response);
            }
        }
        return new InvoiceResponse(
                order.getId(),
                order.getOrderedAt(),
                orderItemsResponse,
                order.getTotalPrice()
        );
    }


    /*Helper Methods*/


    private Customer convertToCustomer(CustomerRegistrationDTO customerRegistrationDTO){
        Customer registeredCustomer=new Customer(customerRegistrationDTO.firstName(), customerRegistrationDTO.lastName(),
                customerRegistrationDTO.phoneNumber());
        Credential credential=new Credential(customerRegistrationDTO.userName(),
                customerRegistrationDTO.password(),customerRegistrationDTO.email(), UserStatus.ACTIVE);

        //attaching the credential to the customer
        registeredCustomer.setCredential(credential);
        //hardcoding the role to be customer
        Role customerRole=new Role(UserRoles.ROLE_CUSTOMER);
        credential.setRole(customerRole );
        customerRole.setCredential(credential);//bi directional link between role and credential
        return registeredCustomer;
    }

    private CustomerProfileResponse updateCustomer(CustomerUpdateProfileRequest request, Customer customer){
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setPhoneNumber(request.phoneNumber());
        customer.setShippingAddress(request.address());
        customer.setImageUrl(request.imageUrl());
        customerRepository.save(customer);
        return new CustomerProfileResponse(customer.getId(),customer.getFirstName(),customer.getLastName(),customer.getPhoneNumber()
                ,customer.getImageUrl(),customer.getShippingAddress(),customer.getCredential().getUserStatus().name());
    }


    private CustomerRegistrationResponse convertToDto(Customer customer){
        CustomerRegistrationResponse responseDTO=new CustomerRegistrationResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getCredential().getEmail(),
                customer.getPhoneNumber(),
                customer.getCredential().getCreatedAt(),
                customer.getCredential().getRole().getRole().name());
        return responseDTO;
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

    private List<CartItem> getCustomerCart(){
        List<CartItem> cart=cartItemRepository.findByCustomerIdWithDetails(getCurrentCustomer().getId());
        if(cart.isEmpty()){
            throw new CartEmptyException("your shopping cart is currently empty");
        }
       return cart;
    }

    private void cartStockValidationAndUpdate(List<CartItem> cart){
        int rowsUpdated=0;
        for(CartItem cartItem:cart){
            rowsUpdated= stockRepository.decrementProductStock(cartItem.getQuantity(),
                    cartItem.getProduct().getId(),
                    cartItem.getProduct().getVendor().getShop().getId());
            if(rowsUpdated==0){
                throw new InsufficientStockException("Product with id: "+cartItem.getProduct().getId()+
                        " is out of stock");
            }
        }
    }

}
