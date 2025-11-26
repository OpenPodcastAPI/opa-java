package org.openpodcastapi.opa.config;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/// Configuration for the web interface
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Additional CSS storage
        registry
                .addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        // Additional JavaScript storage
        registry
                .addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        // The hosted documentation
        registry
                .addResourceHandler("/docs/**")
                .addResourceLocations("classpath:/static/docs/");
    }

    /// Informs Spring to use Thymeleaf Layout Dialect for composing Thymeleaf templates
    ///
    /// See [Thymeleaf Layout Dialect](https://ultraq.github.io/thymeleaf-layout-dialect/) for more information
    ///
    /// @return the configured [LayoutDialect]
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }
}
