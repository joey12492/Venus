package com.babel.venus.config;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.babel.uaa.utils.DomainAppidUtils;

@Component 
@ConfigurationProperties(prefix = "customer")
public class CustomerConfig {
	private final Logger log = LoggerFactory.getLogger(CustomerConfig.class);
	private String runType;
	//forsetti对接payment,cms,config时的配置信息缓存时间，为0表示不缓存
	private Integer cacheInterval;
	/**
	 * 配置见注册中心的git
	 */
	 private Integer lotteryRunSideType;
	 /**
	  * www.baidu.com|plat_owner_test
	  */
	 private List<String> domainAppidBinds;//真实会员平台商appid配置
	 /**
	  * 权限资源配置
	  * exp:/v2/api-docs|ROLE_ADMIN
	  */
	 private List<String> permitResRoles; 
	 
	 @PostConstruct
	 public void init(){
		 log.info("-----lotteryRunSideType="+lotteryRunSideType);
		 log.info("-----domainAppidBinds="+domainAppidBinds
				 +"\n domainAppidMap="+getDomainAppidMap());
		 log.info("-----permitResRoles="+permitResRoles
				 +"\n permitResRoleMap="+getPermitResRoleMap());
	 }

	/**
	 * @return the domainAppidBinds
	 */
	public List<String> getDomainAppidBinds() {
		return domainAppidBinds;
	}

	/**
	 * @param domainAppidBinds the domainAppidBinds to set
	 */
	public void setDomainAppidBinds(List<String> domainAppidBinds) {
		this.domainAppidBinds = domainAppidBinds;
		domainAppidMap= DomainAppidUtils.getDomainAppidListToMap(domainAppidBinds);
	}
	
	private Map<String, String> domainAppidMap=new HashMap<>();
	private Map<String, String> getDomainAppidMap(){
		if(domainAppidMap.isEmpty()){
			domainAppidMap= DomainAppidUtils.getDomainAppidListToMap(domainAppidBinds);
			log.info("-----domainAppidBinds="+domainAppidBinds
					 +"\n domainAppidMap="+domainAppidMap);
		}
		return domainAppidMap;
	}

	


	/**
	 * @return the permitResRoles
	 */
	public List<String> getPermitResRoles() {
		return permitResRoles;
	}

	private Map<String, String> permitResRoleMap=new HashMap<>();
	public Map<String, String> getPermitResRoleMap(){
		if(permitResRoleMap.isEmpty()){
			permitResRoleMap= DomainAppidUtils.getDomainAppidListToMap(permitResRoles);
			log.info("-----permitResRoles="+permitResRoles
					 +"\n permitResRoleMap="+permitResRoleMap);
		}
		return permitResRoleMap;
	}
	/**
	 * @param permitResRoles the permitResRoles to set
	 */
	public void setPermitResRoles(List<String> permitResRoles) {
		this.permitResRoles = permitResRoles;
		permitResRoleMap= DomainAppidUtils.getDomainAppidListToMap(permitResRoles);
	}

	//origin忽略http/https
	public String getAppid(String origin){
		if(origin==null){
			return null;
		}
		origin=origin.trim();
		if(origin.startsWith("http://")){
			origin=origin.substring("http://".length());
		}
		else if(origin.startsWith("https://")){
			origin=origin.substring("https://".length());
		}
		return getDomainAppidMap().get(origin);
	}
	 

	/**
	 * @param lotteryRunSideType the lotteryRunSideType to set
	 */
	public void setLotteryRunSideType(Integer lotteryRunSideType) {
		this.lotteryRunSideType = lotteryRunSideType;
		log.info("-----lotteryRunSideType--"+lotteryRunSideType);
	}


	/**
	 * @return the lotteryRunSideType
	 */
	public Integer getLotteryRunSideType() {
		return lotteryRunSideType;
	}


	public String getRunType() {
		return runType;
	}

	public void setRunType(String runType) {
		this.runType = runType;
	}

	public boolean isRunTypeProd(){
		return "prod".equals(runType);
	}

	/**
	 * @return the cacheInterval
	 */
	public Integer getCacheInterval() {
		return cacheInterval;
	}

	/**
	 * @param cacheInterval the cacheInterval to set
	 */
	public void setCacheInterval(Integer cacheInterval) {
		this.cacheInterval = cacheInterval;
		log.info("-----cacheInterval--"+cacheInterval);
	}
	
	
}
