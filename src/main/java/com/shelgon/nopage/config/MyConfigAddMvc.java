package com.shelgon.nopage.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyConfigAddMvc implements WebMvcConfigurer {


    static final String ORIGINS[] = new String[] { "GET", "POST", "PUT", "DELETE" };

    @Value(value = "${myroom.file.uploadpath}")
    private  String tempPath;//房间文件开放

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        ///创建静态资源路径
        String urlPath=System.getProperty("user.dir");
        String temp;
        //Linux下转换
        if(urlPath.equals(File.separator)){
            temp=tempPath;
        }else{
            temp=urlPath+tempPath;
        }
        File fp=new File(urlPath);
        if(!fp.exists()){
           System.out.println(fp.mkdirs()); ;
        }       
        registry.addResourceHandler("/temp/**").addResourceLocations("file:"+temp);
        registry.addResourceHandler("swagger-ui.html").addResourceLocations(
                "classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

}