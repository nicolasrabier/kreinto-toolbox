package com.kreinto.toolbox;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@EnableWebMvc
@Configuration
@ComponentScan(basePackages = { "com.kreinto.toolbox.controller" })
public class WebMvcConfig implements WebMvcConfigurer {

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/hello").setViewName("hello");
        registry.addViewController("/homepage").setViewName("homepage");
        registry.addViewController("/logout").setViewName("login");
    }



}
