package com.example.dianping;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.dianping.mapper")
@SpringBootApplication
public class DianpingPracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DianpingPracticeApplication.class, args);
    }
}
