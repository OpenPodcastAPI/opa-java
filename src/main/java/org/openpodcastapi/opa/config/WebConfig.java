package org.openpodcastapi.opa.config;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry
                .addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        registry
                .addResourceHandler("/docs/**")
                .addResourceLocations("classpath:/docs/");
    }
}
