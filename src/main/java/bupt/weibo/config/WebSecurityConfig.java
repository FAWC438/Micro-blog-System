package bupt.weibo.config;

import bupt.weibo.Service.CustomUserService;
import bupt.weibo.handler.LoginFailureHandler;
import bupt.weibo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private CustomUserService customUserService;

    @Autowired
    public void setCustomUserService(CustomUserService customUserService) {
        this.customUserService = customUserService;
    }

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
                .antMatchers("/login/**", "/error/**", "/register/**", "/activeUserEmail", "/static/**", "/h2-console/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                // 配置被拦截时的处理
                .and()
                .exceptionHandling()
                //添加无权限时的处理
                .accessDeniedHandler((request, response, e) -> {
                    response.sendRedirect("/error/403?msg=" + e.getMessage());
                })
                .and()
                .formLogin()
                .loginPage("/login")
                .successHandler((request, response, e) -> {
                    System.out.println("登陆成功处理==============");
                    //跳转到首页
                    response.sendRedirect("/");
                })
                .failureHandler(new LoginFailureHandler())
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll();

        //添加账户锁定的处理
        //.authenticationEntryPoint(new LockedAuthenticationEntryPoint());
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

