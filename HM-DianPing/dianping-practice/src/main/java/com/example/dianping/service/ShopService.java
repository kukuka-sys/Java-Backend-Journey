package com.example.dianping.service;

import com.example.dianping.entity.Shop;
import com.example.dianping.utils.Result;

public interface ShopService {
    Result getById(Long id);

    Result getByTypeId(Long typeId, Long current);

    Result updateShop(Shop shop);

    Result getByIdWithLogicalExpire(Long id);

    Result getByIdWithPassThrough(Long id);
}
