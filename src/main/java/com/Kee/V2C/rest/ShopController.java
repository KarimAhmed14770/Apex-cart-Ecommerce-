package com.Kee.V2C.rest;

import com.Kee.V2C.dto.vendor.ShopRegisterRequest;
import com.Kee.V2C.dto.vendor.ShopResponse;
import com.Kee.V2C.dto.vendor.ShopUpdateRequest;
import com.Kee.V2C.dto.vendor.ShopViewResponse;
import com.Kee.V2C.service.shop.ShopService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/shops")
public class ShopController {

    private final ShopService shopService;

    @Autowired
    public ShopController(ShopService shopService){
        this.shopService=shopService;
    }

    @PostMapping
    public ResponseEntity<ShopResponse> registerShop(@RequestBody @Valid ShopRegisterRequest shopRegisterRequest){
        ShopResponse response=shopService.registerShop(shopRegisterRequest);
        URI location= ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping
    public ResponseEntity<ShopResponse> updateShop(@RequestBody @Valid ShopUpdateRequest shopRequest){
        return ResponseEntity.ok(shopService.updateShopInfo(shopRequest));
    }

    @GetMapping
    public ResponseEntity<ShopViewResponse> viewShop(Pageable page){
        return ResponseEntity.ok(shopService.viewShop(page));
    }

    @PatchMapping("/deactivate")
    public ResponseEntity<ShopResponse> deactivateShop(){
        return ResponseEntity.ok(shopService.deactivateShop());
    }

    @PatchMapping("/activate")
    public ResponseEntity<ShopResponse> activateShop(){
        return ResponseEntity.ok(shopService.activateShop());
    }

}
