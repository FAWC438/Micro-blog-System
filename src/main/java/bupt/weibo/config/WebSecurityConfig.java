package bupt.weibo.config;

import bupt.weibo.Service.CustomUserService;
import bupt.weibo.handler.MyAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    CustomUserService customUserService;

    @Override
    public void configure(WebSecurity web) {
        //不拦截静态资源
        web.ignoring().antMatchers("/js/**", "/css/**", "/img/**");
    }

    /***
     * 配置Spring Security
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .headers()
                .frameOptions().disable()
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/login", "/register", "/static/**", "/h2-console/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                // 配置被拦截时的处理 这个位置很关键
                .and()
                .exceptionHandling()
                //添加无权限时的处理
                .accessDeniedHandler(new MyAccessDeniedHandler())
                .and()
                .formLogin()
                .loginPage("/login")
                .successHandler((httpServletRequest, httpServletResponse, authentication) -> {
                    System.out.println("登陆成功处理==============");
                    //跳转到首页
                    httpServletResponse.sendRedirect("/");
                })
                .failureHandler((httpServletRequest, httpServletResponse, e) -> {
                    System.out.println("登陆失败处理=============");
                    //返回到登陆页面
                    httpServletResponse.sendRedirect("/login?error");
                })
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserService).passwordEncoder(new BCryptPasswordEncoder());
    }

//    /***
//     * 在内存中自定义用户
//     *
//     * @param auth
//     * @throws Exception
//     */
//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        BCryptPasswordEncoder MyEncoder = new BCryptPasswordEncoder();
//        auth
//                .inMemoryAuthentication()
//                .passwordEncoder(MyEncoder)
//                .withUser("admin").password(MyEncoder.encode("123456")).roles("USER");
//    }
}

