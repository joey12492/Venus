package com.babel.venus.storage.local;

import com.babel.ares.model.PlaysPO;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * User: joey
 * Date: 2017/11/29
 * Time: 18:25
 */
public class LocalCacheManage {

    /**
     * 所有平台商赔率列表
     * key: platInfoId
     * value: oddsId
     */
    public static Cache<String, Long> platPayoffGroupMapCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(200).build();

    /**
     * 所有平台商下的各个玩法的彩金
     * key: payoffGroup_playId
     * value: payoff
     */
    public static Cache<String, Long> playPayoffMapCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(2000).build();
    /**
     * 返点对照
     * key: payoffGroup_playId
     * varlue: reforwardPoint
     */
    public static Cache<String, Integer> playReforwardMapCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(500).build();

    /**
     * 彩种类型
     */
    public static Cache<Long, Integer> lotteryTypeCache = CacheBuilder.newBuilder()
            .expireAfterAccess(24, TimeUnit.HOURS)
            .maximumSize(30).build();

    public static Cache<String, Long> pdateCache = CacheBuilder.newBuilder()
            .expireAfterAccess(24, TimeUnit.HOURS)
            .maximumSize(30).build();

    public static Cache<Long, Long> acPlatIdCache = CacheBuilder.newBuilder()
            .expireAfterAccess(24, TimeUnit.HOURS)
            .maximumSize(100).build();
}
