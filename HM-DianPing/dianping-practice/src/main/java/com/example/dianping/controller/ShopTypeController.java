package com.example.dianping.controller;

import com.example.dianping.service.ShopTypeService;
import com.example.dianping.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/shop-type")
@RestController
public class ShopTypeController {
    @Autowired
    private ShopTypeService shopTypeService;



    @GetMapping("/list")
    public Result list() {
        return shopTypeService.getList();
    }

}
