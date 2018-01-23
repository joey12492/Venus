package com.babel.venus.web.rest;

import com.babel.venus.config.VenusRefreshConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * User: joey
 * Date: 2017/11/7
 * Time: 10:28
 */
@RestController
public class VenusRefreshConfigResource {

    @Resource
    private VenusRefreshConfig venusRefreshConfig;

    @GetMapping(value = "/help/config/venus_req_draw_hosts", name = "venus开奖后调用的地址")
    public String getVenusReqDrawHosts() {
        return venusRefreshConfig.getVenusReqDrawHosts();
    }

    @GetMapping(value = "/help/config/hermes_req_draw_hosts",name = "hermes开奖后调用的地址")
    public String getHermesReqDrawHosts() {
        return venusRefreshConfig.getHermesReqDrawHosts();
    }
}
