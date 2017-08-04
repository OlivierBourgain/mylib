package com.obourgain.mylib;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.obourgain.mylib.util.img.FileStore;

@Configuration
public class StaticResourceConfiguration extends WebMvcConfigurerAdapter {
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/store/**").addResourceLocations("file:" + FileStore.ROOT);
	}
}