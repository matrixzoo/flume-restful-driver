package com.matrixzoo.flume.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

@Configuration
@EnableWebSecurity
public class IpSecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${security.location.urls}")
    private String urls;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (urls != null) {
            ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = http.authorizeRequests();
            http.csrf().disable();
            String ips[] = urls.split(",");
            StringBuilder strb = new StringBuilder();
            for (int i = 0; i < ips.length; i++) {
                String ip = ips[i];
                strb.append("hasIpAddress('");
                strb.append(ip).append("')");
                if (i < ips.length - 1) strb.append(" or ");
            }
            registry.antMatchers("/**").access(strb.toString());
            registry.and().httpBasic();
        }
    }
}
