package com.gy.cpcsearch.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Liqifeng
 * 用于配置集群中中相关配置信息
 */
@Component
public class ConfUtil {
        @Value("${my-conf.es-ip}")
        public String esIp;

        @Value("${my-conf.es-port}")
        public int esPort;

        @Value("${my-conf.is-sort}")
        public int isSort;

        @Value("${my-conf.sort-field}")
        public String sortField;

        @Value("${my-conf.is-time-filter}")
        public int isTimeFfilter;

        @Value("${my-conf.time-filter-field}")
        public String timeFilterField;


}