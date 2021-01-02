package bupt.weibo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@SpringBootApplication
public class WeiboApplication {


    // 线程存储器 用于定时锁定恢复
    public static ConcurrentHashMap<String, ScheduledFuture<?>> map = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(WeiboApplication.class, args);
        System.out.println("Start Successfully!");
    }

}
