package com.example.dianping.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.dianping.entity.RedisData;
import com.example.dianping.entity.Shop;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.events.Event;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;
    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    //redis缓存
    public void set(String key, Object value, Long time, TimeUnit timeUnit) {
            String jsonStr= JSONUtil.toJsonStr(value);
            stringRedisTemplate.opsForValue().set(key, jsonStr, time, timeUnit);

    }
    //lua脚本引入
    private static final DefaultRedisScript<Long> script = new DefaultRedisScript<>();
    static {
        script.setLocation(new ClassPathResource("lock.lua"));
        script.setResultType(Long.class);
    }
    //线程池设置
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);


    //封装逻辑过期
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit timeUnit) {
        RedisData redisData = new RedisData();

        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));

        String jsonStr= JSONUtil.toJsonStr(redisData);
        stringRedisTemplate.opsForValue().set(key, jsonStr);


    }

    //缓存穿透
    public <R,ID>R queryWithPassThrough(String key, Class<R> clazz, ID id, Function<ID,R> dbFallback, Long time, TimeUnit timeUnit) {

        key=key+id;
        String jsonStr = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(jsonStr)){
            return JSONUtil.toBean(jsonStr, clazz);

        }

        if (jsonStr != null) {
            return null;
        }
        R apply = dbFallback.apply(id);
        if(apply == null){
            stringRedisTemplate.opsForValue().set(key, "",2, TimeUnit.MINUTES);
            return null;
        }
//        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(apply), time, timeUnit);
        this.set(key, apply, time, timeUnit);

        return apply;



    }

    //获取锁
    private String tryLock(String lockey){
        UUID uuid = UUID.randomUUID();
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(lockey, uuid.toString(),30L, TimeUnit.SECONDS);
        if(!BooleanUtil.isTrue(success)){
            return null;
        }
        return uuid.toString();

    }

    //释放锁
    private void unlock(String lockey, String lockValue){
         stringRedisTemplate.execute(
                script,
                Collections.singletonList(lockey),
                lockValue


        );
    }

    //缓存击穿
    public <R,ID> R queryWithMutex(String key, Class<R> clazz, ID id, Function<ID,R> dbFallback, Long time, TimeUnit timeUnit) {
        key=key+id;
        String lockey="lock:"+key;

        while(true){
            String jsonStr = stringRedisTemplate.opsForValue().get(key);
            if(StrUtil.isNotBlank(jsonStr)){
                return JSONUtil.toBean(jsonStr, clazz);
            }
            if (jsonStr != null) {
                return null;
            }
            String uuid = this.tryLock(lockey);


                if(uuid == null){
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("等待缓存重建时被中断", e);
                    }
                    continue;

                }
            try {
                String result = stringRedisTemplate.opsForValue().get(key);
                if(StrUtil.isNotBlank(result)){
                    return JSONUtil.toBean(result, clazz);

                }
                if (result != null) {
                    return null;
                }
                R apply = dbFallback.apply(id);
                if(apply == null){
                    stringRedisTemplate.opsForValue().set(key, "",2, TimeUnit.MINUTES);
                    return null;
                }
                this.set(key, apply, time, timeUnit);
                return apply;

            }finally {

                this.unlock(lockey,uuid);

            }


        }

    }
    public <R,ID> R queryWithLogicalExpire(String key, Class<R> clazz, ID id,Function<ID,R> dbFallback, Long time, TimeUnit timeUnit) {
        key=key+id;
        String lockey="lock:"+key;

            //防止缓存穿透
            String Jsonstr = stringRedisTemplate.opsForValue().get(key);
            if (!StrUtil.isNotBlank(Jsonstr)) {
                if (Jsonstr != null) {
                    return null;
                }

                //查询数据库没有就缓存空指，有就直接返回
                R apply = dbFallback.apply(id);
                if (apply == null) {
                    stringRedisTemplate.opsForValue().set(key, "",2, TimeUnit.MINUTES);
                    return null;
                }
                setWithLogicalExpire(key, apply, time, timeUnit);
                return apply;


            }

            //判断逻辑是否过期
            RedisData redisData = JSONUtil.toBean(Jsonstr, RedisData.class);
            R oldData = BeanUtil.toBean(redisData.getData(), clazz);
            if (LocalDateTime.now().isBefore(redisData.getExpireTime())) {
            return oldData;
            }


            //异步重建
            String success = this.tryLock(lockey);
            if(success != null){String finalKey = key;
                executorService.execute(() -> {
                    try {
                        R newData= dbFallback.apply(id);
                        if(newData == null){
                            stringRedisTemplate.delete(finalKey);
                            return;
                        }
                        setWithLogicalExpire(finalKey,newData,time,timeUnit);
                    }finally {
                        unlock(lockey,success);
                    }

                });

            }



            //先返回旧数据
            return oldData;







    }


}
