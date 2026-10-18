package com.example.dianping.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dianping.entity.RedisData;
import com.example.dianping.entity.Shop;
import com.example.dianping.mapper.ShopMapper;
import com.example.dianping.service.ShopService;
import com.example.dianping.utils.CacheClient;
import com.example.dianping.utils.RedisConstant;
import com.example.dianping.utils.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ShopServiceImpl implements ShopService {
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private CacheClient cacheClient;



    //lua脚本引入
    private static final DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>();
    static {
        defaultRedisScript.setLocation(new ClassPathResource("lock.lua"));
        defaultRedisScript.setResultType(Long.class);
    }

    //线程池创建
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);



    @Override
    public Result getById(Long id) {
        Shop shop = cacheClient.queryWithMutex(RedisConstant.SHOP_INFO_KEY,
                Shop.class, id,
                shopMapper::selectById,
                RedisConstant.SHOP_INFO_TTL,
                TimeUnit.MINUTES
        );
        if (shop == null) {
            return Result.fail("店铺不存在");
        }



        return Result.ok(shop);

//        String key= RedisConstant.SHOP_INFO_KEY+id;
//        Shop shop;
//        for (int i = 0; i < 10; i++) {
//            String str = stringRedisTemplate.opsForValue().get(key);
//            if (StrUtil.isNotBlank(str)) {
//                 shop = JSONUtil.toBean(str, Shop.class);
//                return Result.ok(shop);
//            }
//
//            if (str != null) {
//                return Result.fail("店铺不存在");
//            }
//
//            String lockey = "lock:shop:" + id;
//            String lockValue= UUID.randomUUID().toString();
//            Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(lockey, lockValue, 30, TimeUnit.SECONDS);
//            if (!Boolean.TRUE.equals(lock)) {
//                try {
//                    Thread.sleep(50);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    return Result.fail("请求被中断");
//                }
//                continue;
//
//
//            }
//
//
//            try {
//                String str1 = stringRedisTemplate.opsForValue().get(key);
//                if (StrUtil.isNotBlank(str1)) {
//                    return Result.ok(JSONUtil.toBean(str1, Shop.class));
//
//                }
//                if (str1 != null) {
//                    return Result.fail("店铺不存在");
//                }
//
//
//                shop = shopMapper.selectById(id);
//                if (shop == null) {
//                    String value = "";
//                    stringRedisTemplate.opsForValue().set(key, value, RedisConstant.SHOP_NULL_TTL, TimeUnit.MINUTES);
//                    return Result.fail("店铺不存在");
//                }
//                String shopStr = JSONUtil.toJsonStr(shop);
//                Long random = RandomUtil.randomLong(5);
//                stringRedisTemplate.opsForValue().set(key, shopStr, RedisConstant.SHOP_INFO_TTL + random, TimeUnit.MINUTES);
//                return Result.ok(shop);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            } finally {
//
//
//                stringRedisTemplate.execute(
//                        defaultRedisScript,
//                        Collections.singletonList(lockey),
//                        lockValue);
//
//
//
//
//            }
//
//
//        }

//        Shop shop = shopMapper.selectById(id);
//        if(shop==null){
//            String value="";
//            stringRedisTemplate.opsForValue().set(key,value,RedisConstant.SHOP_NULL_TTL,TimeUnit.MINUTES);
//            return Result.fail("店铺不存在");
//        }
//        String shopStr = JSONUtil.toJsonStr(shop);
//        Long random= RandomUtil.randomLong(5);
//        stringRedisTemplate.opsForValue().set(key, shopStr,RedisConstant.SHOP_INFO_TTL+random, TimeUnit.MINUTES);



    }

    @Override
    public Result getByTypeId(Long typeId, Long current) {
        Page<Shop> page = new Page<>(current, 5);
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getTypeId, typeId).orderByAsc(Shop::getId);
        shopMapper.selectPage(page, wrapper);
        return Result.ok(page.getRecords());
    }

    @Override
    public Result updateShop(Shop shop) {
        if (shop==null||shop.getId() == null) {
            return Result.fail("店铺信息不能为空");

        }
        int lines = shopMapper.updateById(shop);
        if (lines == 0) {
            return Result.fail("店铺不存在");
        }
        stringRedisTemplate.delete(RedisConstant.SHOP_INFO_KEY+shop.getId());
        stringRedisTemplate.delete("shop:logical:" + shop.getId());
        return Result.ok();
    }
    private void saveShopWithLogicalExpire(Long id,Long minutes){
        Shop shop=shopMapper.selectById(id);
        if(shop==null){
            stringRedisTemplate.delete("shop:logical:"+id);
            return;
        }
//        RedisData redisData=new RedisData();
//        redisData.setData(shop);
//        redisData.setExpireTime(LocalDateTime.now().plusMinutes(minutes));
//        String jsonStr = JSONUtil.toJsonStr(redisData);
//        stringRedisTemplate.opsForValue().set("shop:logical:"+id,jsonStr);

        cacheClient.setWithLogicalExpire("shop:logical:"+id,shop,minutes,TimeUnit.MINUTES);

    }

    //逻辑过期实现
    @Override
    public Result getByIdWithLogicalExpire(Long id){
        Shop shop = cacheClient.queryWithLogicalExpire("shop:logical:",
                Shop.class,
                id,
                shopMapper::selectById,
                RedisConstant.SHOP_INFO_TTL,
                TimeUnit.MINUTES);


        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);

    }


    @Override
    public Result getByIdWithPassThrough(Long id) {
        Shop shop = cacheClient.queryWithPassThrough(
                RedisConstant.SHOP_INFO_KEY,
                Shop.class,
                id,
                shopMapper::selectById,
                RedisConstant.SHOP_INFO_TTL,
                TimeUnit.MINUTES
        );

        if (shop == null) {
            return Result.fail("店铺不存在");
        }

        return Result.ok(shop);
    }
}
