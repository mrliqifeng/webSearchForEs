package com.gy.cpcsearch;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

@MapperScan(value = "com.gy.cpcsearch.mapper")
public class SearchApplication {
    //热点数据统计任务运行类
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }
}