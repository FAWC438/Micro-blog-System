package bupt.weibo.controller;

import bupt.weibo.entity.WeiboUser;
import bupt.weibo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @GetMapping("/login/403")
    String error403() {
        return "403";
    }

    @GetMapping("/login/401")
    String error401(String msg, String username) {
        if (!msg.equals("User account is locked")) {
            WeiboUser user = userRepository.findByUsername(username);
            user.setAccountNonLocked(false);
            userRepository.save(user);
        }
        return "401";
    }
}
