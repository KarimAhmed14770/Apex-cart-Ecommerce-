package com.Kee.V2C.service.shop;

import com.Kee.V2C.dto.vendor.ShopRegisterRequest;
import com.Kee.V2C.dto.vendor.ShopResponse;
import com.Kee.V2C.dto.vendor.ShopUpdateRequest;
import com.Kee.V2C.dto.vendor.ShopViewResponse;
import org.springframework.data.domain.Pageable;

public interface ShopService {
    ShopResponse registerShop(ShopRegisterRequest shopRegisterRequest);
    ShopResponse updateShopInfo(ShopUpdateRequest shopRequest);
    ShopResponse deactivateShop();
    ShopResponse activateShop();
    ShopViewResponse viewShop(Pageable page);
}
