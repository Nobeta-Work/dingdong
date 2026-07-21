package cn.nobeta.dingdong.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;

@SpringBootApplication
@MapperScan("cn.nobeta.dingdong.user.mapper")
@EnableDubbo(scanBasePackages = "cn.nobeta.dingdong.user.rpc")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
