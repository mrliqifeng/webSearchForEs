package com.gy.cpcsearch;

import com.gy.cpcsearch.mapper.FieldInfoMapper;
import com.gy.cpcsearch.service.FieldInfoService;
import com.gy.cpcsearch.service.TableInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class SearchApplicationTests {
    @Autowired
    FieldInfoService fieldInfoService;

    @Autowired
    FieldInfoMapper fieldInfoMapper;

    @Autowired
    TableInfoService tableInfoService;

    @Test
    void tagResultMappercontextLoads() {
        System.out.println(fieldInfoService.findTagInfoByTableName("g01_attacklog_detail"));
    }

}
