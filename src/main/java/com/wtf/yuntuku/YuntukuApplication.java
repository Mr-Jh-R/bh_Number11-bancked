package com.wtf.yuntuku;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.wtf.yuntuku.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class YuntukuApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuntukuApplication.class, args);
    }

}
