package com.gugugu.haochat.common;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication(scanBasePackages = {"com.gugugu.haochat"})
@MapperScan({"com.gugugu.haochat.common.**.mapper"})
@ServletComponentScan
public class HaoChatCustomApplication {
    public static void main(String[] args) {
        SpringApplication.run(HaoChatCustomApplication.class,args);
    }

}
