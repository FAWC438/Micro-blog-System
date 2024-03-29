package bupt.weibo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    /***
     * 初始化控制器
     *
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("mainPage");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/register").setViewName("register");
        registry.addViewController("/mainPage").setViewName("mainPage");
        registry.addViewController("/error/404").setViewName("404");
        registry.addViewController("/error/403").setViewName("403");
        registry.addViewController("/error/401").setViewName("401");
    }

    /***
     * 使得静态资源能够正常访问
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/resources/")
                .addResourceLocations("classpath:/static/")
                .addResourceLocations("classpath:/public/");
        registry.addResourceHandler("/uploadPic/**").addResourceLocations("file:D:\\code\\WebFinal\\src\\main\\resources\\static\\picData\\");
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }

}
