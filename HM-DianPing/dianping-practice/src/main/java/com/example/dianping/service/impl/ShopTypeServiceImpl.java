package com.example.dianping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dianping.entity.ShopType;
import com.example.dianping.mapper.ShopTypeMapper;
import com.example.dianping.service.ShopTypeService;
import com.example.dianping.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopTypeServiceImpl implements ShopTypeService {

    @Autowired
    private ShopTypeMapper shopTypeMapper;

    @Override
    public Result getList() {
       LambdaQueryWrapper<ShopType> queryWrapper = new LambdaQueryWrapper<>();
       queryWrapper.orderByAsc(ShopType::getSort);
       List<ShopType> shopTypeList = shopTypeMapper.selectList(queryWrapper);
       return Result.ok(shopTypeList);


    }
}
