package com.babel.venus.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * User: joey
 * Date: 2017/11/7
 * Time: 10:28
 * 通过springCloud配置中心管理的可以刷新的配置
 */
@RefreshScope
@Component
public class VenusRefreshConfig {


    @Value("${customer.venus.draw.prize.hosts}")
    private String venusReqDrawHosts;
    @Value("${customer.hermes.draw.prize.hosts}")
    private String hermesReqDrawHosts;

    /**
     * venus开奖后调用的地址
     * @return
     */
    public String getVenusReqDrawHosts() {
        return venusReqDrawHosts;
    }

    /**
     * hermes开奖后调用的地址
     * @return
     */
    public String getHermesReqDrawHosts() {
        return hermesReqDrawHosts;
    }
}
