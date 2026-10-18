package com.example.dianping.controller;

import com.example.dianping.entity.Shop;
import com.example.dianping.service.ShopService;
import com.example.dianping.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop")
public class ShopController {
    @Autowired
    private ShopService shopService;


    @GetMapping("{id}")
    public Result getById(@PathVariable Long id) {
        return shopService.getById(id);
    }


    @GetMapping("/of/type")
    public Result getByTypeId(@RequestParam Long typeId, @RequestParam Long current) {

        return shopService.getByTypeId(typeId,current);
    }
    @PutMapping("/update")
    public Result updateShop(@RequestBody Shop shop) {
        return shopService.updateShop(shop);
    }

    @GetMapping("/logical/{id}")
    public Result getByIdWithLogicalExpire(@PathVariable Long id) {
        return shopService.getByIdWithLogicalExpire(id);
    }

    @GetMapping("/pass-through/{id}")
    public Result getByIdWithPassThrough(@PathVariable Long id) {
        return shopService.getByIdWithPassThrough(id);
    }

}
