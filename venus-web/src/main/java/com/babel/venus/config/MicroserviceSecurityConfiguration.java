package com.babel.venus.config;


import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.client.RestTemplate;

import com.babel.uaa.filter.WebRequestFilter;
import com.babel.uaa.security.AuthoritiesConstants;
import com.babel.venus.constants.VenusServer;

import io.github.jhipster.config.JHipsterProperties;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MicroserviceSecurityConfiguration extends ResourceServerConfigurerAdapter {

    private final JHipsterProperties jHipsterProperties;

    private final DiscoveryClient discoveryClient;
    @Autowired
    private CustomerConfig customerConfig;

    public MicroserviceSecurityConfiguration(JHipsterProperties jHipsterProperties,
            DiscoveryClient discoveryClient) {

        this.jHipsterProperties = jHipsterProperties;
        this.discoveryClient = discoveryClient;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
    	ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry antMatches=
        http
            .csrf()
            .disable()
            .headers()
            .frameOptions()
            .disable()
        .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .authorizeRequests()
            .antMatchers("/api/profile-info").permitAll()
            .antMatchers("/api/**").authenticated()
            .antMatchers(VenusServer.DRAW_PRIZE_INTERNAL).permitAll()
//            .antMatchers(WebRequestFilter.removeEndSlash(VenusServer.DRAW_PRIZE_INTERNAL)+"/").permitAll()
            .antMatchers("/v2/api-docs").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/apis/**").authenticated()
            .antMatchers("/management/health").permitAll()
            .antMatchers("/management/**").permitAll()
            .antMatchers("/swagger-resources/configuration/ui").permitAll();
    	
//    	AuthoritiesConstants.initConfigMatches(customerConfig.getPermitResRoleMap(), antMatches);
    }

    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter(
            @Qualifier("loadBalancedRestTemplate") RestTemplate keyUriRestTemplate) {

        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setVerifierKey(getKeyFromAuthorizationServer(keyUriRestTemplate));
        return converter;
    }

    @Bean
    public RestTemplate loadBalancedRestTemplate(RestTemplateCustomizer customizer) {
        RestTemplate restTemplate = new RestTemplate();
        customizer.customize(restTemplate);
        return restTemplate;
    }

    private String getKeyFromAuthorizationServer(RestTemplate keyUriRestTemplate) {
        // Load available UAA servers
        discoveryClient.getServices();
        HttpEntity<Void> request = new HttpEntity<Void>(new HttpHeaders());
        return (String) keyUriRestTemplate
            .exchange("http://uaa/oauth/token_key", HttpMethod.GET, request, Map.class).getBody()
            .get("value");

    }
    
    @Value("${customer.oauth2.tokenCheckStatus}")
    private String tokenCheckStatus;
    /**
     * token过期，下线，重复登入处理
     * @return
     */
    @Bean
    public FilterRegistrationBean filterRegistration() {
    	tokenCheckStatus="false";
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new WebRequestFilter(tokenCheckStatus, this.customerConfig.getPermitResRoleMap(), Arrays.asList(VenusServer.DRAW_PRIZE_INTERNAL)));
        registration.addUrlPatterns("/*");
//        registration.addInitParameter("paramName", "paramValue");
        registration.setName("webRequestFilter");
        registration.setOrder(1);
        return registration;
    }
}
