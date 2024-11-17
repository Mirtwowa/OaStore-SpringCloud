package org.oastore;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("org.oastore.mapper")
@EnableDiscoveryClient
@EnableFeignClients
@EnableHystrix
@Slf4j
public class OaStorePayApplication {
    public static void main(String[] args) {
        SpringApplication.run(OaStorePayApplication.class, args);
    }
}