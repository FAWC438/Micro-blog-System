//package bupt.weibo.handler;
//
//import bupt.weibo.Service.CustomUserService;
//import bupt.weibo.WeiboApplication;
//import bupt.weibo.entity.WeiboUser;
//import bupt.weibo.repository.UserRepository;
//import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
//import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
//import es.moki.ratelimitj.inmemory.InMemorySlidingWindowRequestRateLimiter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationListener;
//import org.springframework.context.annotation.Bean;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
//import org.springframework.scheduling.support.CronTrigger;
//import org.springframework.security.authentication.LockedException;
//import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
//import org.springframework.stereotype.Component;
//
//import java.util.Collections;
//import java.util.Set;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.TimeUnit;
//
//@Component
//public class LoginFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
//    @Autowired
//    UserRepository userRepository;
//
//    @Autowired
//    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
//
//    @Autowired
//    CustomUserService userService;
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(LoginFailureListener.class);
//
//    //规则定义：5分钟之内3次机会，就触发限流行为
//    Set<RequestLimitRule> rules =
//            Collections.singleton(RequestLimitRule.of(5, TimeUnit.MINUTES, 3));
//    RequestRateLimiter limiter = new InMemorySlidingWindowRequestRateLimiter(rules);
//
//    /**
//     * ThreadPoolTaskScheduler：线程池任务调度类，能够开启线程池进行任务调度。
//     * ThreadPoolTaskScheduler.schedule()方法会创建一个定时计划ScheduledFuture，在这个方法需要添加两个参数，Runnable（线程接口类） 和CronTrigger（定时任务触发器）
//     *
//     * @return
//     */
//    @Bean
//    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
//        return new ThreadPoolTaskScheduler();
//    }
//
//    @Override
//    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
//        if (event.getException() instanceof LockedException) {
//            String userName = event.getAuthentication().getName();
//            System.out.println(userName);
//
//            WeiboUser user = (WeiboUser) userService.loadUserByUsername(userName);
//            //user.setLockCount(user.getLockCount() + 1);
//            //计数器加1，并判断该用户是否已经到了触发了锁定规则
//            boolean reachLimit = limiter.overLimitWhenIncremented(userName);
//
//            if (reachLimit) {
//                //如果触发了锁定规则，通过UserDetails告知Spring Security锁定账户
//                System.out.println("Locked");
//                user.setAccountNonLocked(false);
//                System.out.println(user.isAccountNonLocked());
//                System.out.println(user.getId());
//                userRepository.save(user);
//                LOGGER.info("user:{} is locked", user);
//                if ((WeiboApplication.map.get(user.getUsername()) == null || WeiboApplication.map.get(user.getUsername()).isCancelled())) {
//                /*
//                  在ScheduledFuture中有一个cancel可以停止定时任务。
//                 */
//                    ScheduledFuture<?> future = threadPoolTaskScheduler.schedule(new UserUnlock(user.getUsername()), new CronTrigger("* * 1/1 * * ?"));
//                    assert future != null;
//                    WeiboApplication.map.put(user.getUsername(), future);
//                }
//            }
//        }
//    }
//
//    private class UserUnlock implements Runnable {
//
//        private final String userName;
//
//        public UserUnlock(String userName) {
//            this.userName = userName;
//        }
//
//        public void run() {
//
//            WeiboUser user = (WeiboUser) userService.loadUserByUsername(userName);
//            LOGGER.info("线程user:{} thread is running", user);
//            user.setAccountNonLocked(true);
//            //user.setLockCount(0);
//            userRepository.save(user);
//            WeiboApplication.map.get(user.getUsername()).cancel(true);
//        }
//    }
//}
