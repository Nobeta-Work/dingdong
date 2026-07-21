package cn.nobeta.dingdong.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;

@SpringBootApplication
@MapperScan("cn.nobeta.dingdong.order.mapper")
@EnableScheduling
@EnableDubbo(scanBasePackages = "cn.nobeta.dingdong.order.rpc")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
