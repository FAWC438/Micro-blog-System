package bupt.weibo.Service;

import bupt.weibo.repository.UserRepository;
import bupt.weibo.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.sql.Timestamp;
import java.util.UUID;

@Service
public class AsyncSendEmailService {
    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Resource
    JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String mailFrom;


    @Async   //这是一个异步方法
    public void sendVerifyEmail(String email, String action) {
        try {
            Thread.sleep(1000);
            String secretKey = UUID.randomUUID().toString(); // 密钥
            Timestamp outDate = new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000);// 30分钟后过期
            long date = outDate.getTime() / 1000 * 1000;
            userRepository.setEmailOutDateAndValidCode(outDate + "", secretKey, email);
            String mailActiveContent = "请点击以下链接激活你的账号，30分钟内有效\n";
            String mailActiveSubject = "Weibo账号邮箱注册验证";
            if (action.equals("resetUserPassword")) {
                mailActiveContent = "请点击以下链接重置你的账号密码，30分钟内有效\n";
                mailActiveSubject = "Weibo账号密码重置";
            }

            String key = email + "$" + date + "$" + secretKey;

            String digitalSignature = MD5Util.encode(key);// 数字签名
            String activeUserUrl = "http://localhost:8080/" + action;

            String resetPassHref = activeUserUrl + "?sid="
                    + digitalSignature + "&email=" + email;

            String emailContent = mailActiveContent + resetPassHref;
            System.out.println(emailContent);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(mailFrom);
            helper.setTo(email);

            helper.setSubject(mailActiveSubject);
            helper.setText(emailContent, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(email);
    }
}
