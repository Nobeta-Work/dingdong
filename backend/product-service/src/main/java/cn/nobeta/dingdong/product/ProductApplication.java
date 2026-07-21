package cn.nobeta.dingdong.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;

@SpringBootApplication
@MapperScan("cn.nobeta.dingdong.product.mapper")
@EnableDubbo(scanBasePackages = "cn.nobeta.dingdong.product.rpc")
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
