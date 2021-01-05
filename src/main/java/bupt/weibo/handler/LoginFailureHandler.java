package bupt.weibo.handler;

import bupt.weibo.WeiboApplication;
import bupt.weibo.entity.WeiboUser;
import bupt.weibo.repository.UserRepository;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.inmemory.InMemorySlidingWindowRequestRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

// DELETE FROM CUSTOMER;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private static UserRepository userRepository;
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        LoginFailureHandler.userRepository = userRepository;
    }

    @Autowired
    public void setThreadPoolTaskScheduler(ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    }

//    @Autowired
//    private CustomUserService userService;

    //规则定义：5分钟之内3次机会，就触发限流行为
    Set<RequestLimitRule> rules =
            Collections.singleton(RequestLimitRule.of(5, TimeUnit.MINUTES, 3));
    RequestRateLimiter limiter = new InMemorySlidingWindowRequestRateLimiter(rules);


    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
        System.out.println("登陆失败处理=============");
        System.out.println(e.toString());
        String userName = request.getParameter("username");
        System.out.println(userName);

        WeiboUser user = userRepository.findByUsername(userName);
        if (user == null) {
            response.sendRedirect("/login?error");
            // throw new UsernameNotFoundException("用户名不存在");
            return;
        }

        // System.out.println(user);
        //user.setLockCount(user.getLockCount() + 1);

        //计数器加1，并判断该用户是否已经到了触发了锁定规则
        boolean reachLimit = limiter.overLimitWhenIncremented(userName);

        if (reachLimit) {
            //如果触发了锁定规则，通过UserDetails告知Spring Security锁定账户
            System.out.println("开始Locked！！！！");
            user.setAccountNonLocked(false);
            userRepository.save(user);

            if ((WeiboApplication.map.get(user.getUsername()) == null || WeiboApplication.map.get(user.getUsername()).isCancelled())) {
                /*
                  在ScheduledFuture中有一个cancel可以停止定时任务。
                 */
                ScheduledFuture<?> future = threadPoolTaskScheduler.schedule(new UserUnlock(user.getUsername()), new CronTrigger("0 0 1/1 * * ?"));
                assert future != null;
                WeiboApplication.map.put(user.getUsername(), future);
            }
            //response.sendRedirect("/error/401?msg=" + e.getMessage() + "&username=" + userName);
            response.sendRedirect("/error/401");
            return;
        }
        if (e instanceof LockedException) {
            response.sendRedirect("/error/401?msg=" + e.getMessage() + "&username=" + userName);
        } else {
            response.sendRedirect("/login?error");
        }
    }

    private static class UserUnlock implements Runnable {

        private final String userName;

        public UserUnlock(String userName) {
            this.userName = userName;
        }

        public void run() {

            WeiboUser user = userRepository.findByUsername(userName);
            if (user == null) {
                throw new UsernameNotFoundException("用户名不存在");
            }
            user.setAccountNonLocked(true);
            //user.setLockCount(0);
            userRepository.save(user);
            WeiboApplication.map.get(user.getUsername()).cancel(true);
        }
    }
}
