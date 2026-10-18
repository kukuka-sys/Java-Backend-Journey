package com.example.dianping.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dianping.dto.LoginFromDTO;
import com.example.dianping.dto.UserDTO;
import com.example.dianping.entity.User;
import com.example.dianping.mapper.UserMapper;
import com.example.dianping.service.UserService;
import com.example.dianping.utils.RedisConstant;
import com.example.dianping.utils.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserMapper userMapper;



    @Override
    public Result sendCode(String phone) {
        boolean valid = phone != null && phone.matches("^1[3-9]\\d{9}$");
        if(StrUtil.isBlank(phone)||!valid){
            return Result.fail("手机号错误");
        }
        String code =RandomUtil.randomNumbers(6);

        stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_CODE_KEY+phone, code,
                RedisConstant.LOGIN_CODE_TTL, TimeUnit.SECONDS);
        log.debug("验证码为：{}",code);


        return Result.ok();
    }

    @Override
    public Result login(LoginFromDTO loginFromDTO) {
        if(loginFromDTO==null){
            return Result.fail("用户信息不为空");
        }


        String code = stringRedisTemplate.opsForValue().get(RedisConstant.LOGIN_CODE_KEY + loginFromDTO.getLoginName());
        if (code == null || !code.equals(loginFromDTO.getCode())) {
            return Result.fail("验证码错误");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>();
        queryWrapper.eq(User::getLoginName,loginFromDTO.getLoginName());
        User user =userMapper.selectOne(queryWrapper);
        if(user==null){
            user= BeanUtil.copyProperties(loginFromDTO,User.class);
            user.setNickName(UUID.randomUUID().toString().replace("-",""));
           userMapper.insert(user);
        }
        UserDTO userDTO = BeanUtil.copyProperties(user,UserDTO.class);



        String token = UUID.randomUUID().toString().replace("-", "");

        Map<String,Object> map =BeanUtil.beanToMap(userDTO,new HashMap<>(),CopyOptions.create()
                .setIgnoreNullValue(true)
                .setFieldValueEditor((fieldName,fieldValue)->fieldValue.toString()));

        stringRedisTemplate.opsForHash().putAll(RedisConstant.LOGIN_INFO_KEY+token,map);

        stringRedisTemplate.expire(RedisConstant.LOGIN_INFO_KEY+token,RedisConstant.LOGIN_TOKEN_TTL, TimeUnit.MINUTES);


        stringRedisTemplate.delete(RedisConstant.LOGIN_CODE_KEY+loginFromDTO.getLoginName());
        return Result.ok(token);




    }
}
