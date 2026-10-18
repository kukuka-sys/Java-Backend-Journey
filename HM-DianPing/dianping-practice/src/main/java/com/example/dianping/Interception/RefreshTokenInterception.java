package com.example.dianping.Interception;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.example.dianping.dto.UserDTO;
import com.example.dianping.utils.RedisConstant;
import com.example.dianping.utils.UserHolder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RefreshTokenInterception implements HandlerInterceptor {

    @Resource
    private StringRedisTemplate stringRedisTemplate;



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //根据请求体解析出token
        String token = request.getHeader("authorization");
        //判断token是否为空，是就代表用户未登录放行
        if(StrUtil.isBlank(token)){
            return true;
        }

        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(RedisConstant.LOGIN_INFO_KEY+token);
        if(userMap.isEmpty()){
            return true;
        }


        UserDTO userDTO1 = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        UserHolder.set(userDTO1);

        stringRedisTemplate.expire(RedisConstant.LOGIN_INFO_KEY+token,RedisConstant.LOGIN_TOKEN_TTL, TimeUnit.MINUTES);


        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.remove();
    }
}
