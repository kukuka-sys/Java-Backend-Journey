package com.example.dianping.controller;

import cn.hutool.core.util.StrUtil;
import com.example.dianping.dto.LoginFromDTO;
import com.example.dianping.dto.UserDTO;
import com.example.dianping.service.UserService;
import com.example.dianping.utils.RedisConstant;
import com.example.dianping.utils.Result;
import com.example.dianping.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserService userService;

    @PostMapping("/code")
    public Result sendCode(@RequestParam String phone){
       return userService.sendCode(phone);

    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginFromDTO loginFromDTO) {
       return userService.login(loginFromDTO);

    }

    @GetMapping("/me")
    public Result me() {

        return Result.ok(UserHolder.get());
    }


    @PostMapping("/logout")
    public Result logout(@RequestHeader("authorization") String token) {
        stringRedisTemplate.delete(RedisConstant.LOGIN_INFO_KEY+token);
        return Result.ok();
    }


}
