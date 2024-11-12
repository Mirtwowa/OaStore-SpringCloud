package org.example.oastoreaop;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("org.example.oastoreaop.mapper")
@EnableAsync
public class OaStoreAopApplication {
    public static void main(String[] args) {
        SpringApplication.run(OaStoreAopApplication.class, args);
    }
}
