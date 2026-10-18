package com.example.dianping.config;

import com.example.dianping.Interception.LoginInterception;
import com.example.dianping.Interception.RefreshTokenInterception;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {


    @Resource
    private RefreshTokenInterception refreshTokenInterception;
    @Resource
    private LoginInterception loginInterception;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(refreshTokenInterception).addPathPatterns("/**").order(0);


        registry.addInterceptor(loginInterception).excludePathPatterns(
                "/error",        // 保留框架生成的原始错误状态，避免被改写成 401
                "/api/health",   // 基础健康检查无需登录
                "/user/login",   // 登录接口本身，还没登录自然要放行
                "/user/code",                 // 发送验证码接口，同上
                "/blog/hot",                  // 首页热门笔记，不登录也要能看
                "/shop/**",                   // 店铺的浏览、搜索、按分类查，属于游客可见内容
                "/voucher/**",                // 优惠券查询、秒杀券列表
                "/shop-type/**"             // 店铺分类列表（首页左侧导航）
                // 图片上传/读取回显（课程里为了演示方便放开了）



        ).order(1);
    }
}
