package bupt.weibo.controller;

import bupt.weibo.Service.AsyncSendEmailService;
import bupt.weibo.entity.WeiboRole;
import bupt.weibo.entity.WeiboUser;
import bupt.weibo.repository.RoleRepository;
import bupt.weibo.repository.UserRepository;
import bupt.weibo.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/")
public class RegisterController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    AsyncSendEmailService asyncSendEmailService;
    @Autowired
    AmqpTemplate rabbitTemplate;

    @Resource
    private JavaMailSender mailSender;


    @GetMapping("/register")
    String register() {
        return "register";
    }

//    @PostMapping("/register")
//    String checkRegister(Model model, String username, String password, RedirectAttributes redirectAttributes) {
//        WeiboUser weiboUser = userRepository.findByUsername(username);
//        if (weiboUser != null) {
//            model.addAttribute("msg", "用户名已存在！");
//            return "register";
//        } else {
//            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//            weiboUser = new WeiboUser();
//            weiboUser.setUsername(username);
//            weiboUser.setPassword(passwordEncoder.encode(password));
//            weiboUser.setAccountNonLocked(true);
//            WeiboRole weiboRole = new WeiboRole();
//            if (username.equals("admin")) {
//                weiboRole.setRoleName("ROLE_ADMIN");
//            } else {
//                weiboRole.setRoleName("ROLE_USER");
//            }
//            roleRepository.save(weiboRole);
//            List<WeiboRole> roles = new ArrayList<>();
//            roles.add(weiboRole);
//            weiboUser.setRoles(roles);
//            userRepository.save(weiboUser);
//            redirectAttributes.addFlashAttribute("alertMsg", "注册成功！");
//            return "redirect:/login";
//        }
//    }

    @PostMapping("/register")
    String registerByEmail(Model model, WeiboUser user, RedirectAttributes redirectAttributes) {
        try {
            System.out.println(user.getEmail());
            WeiboUser registerUser = userRepository.findByEmail(user.getEmail());
            if (null != registerUser) {
                model.addAttribute("msg", "用户邮箱已存在！");
                return "register";
            }
            WeiboUser userNameUser = userRepository.findByUsername(user.getUsername());
            if (null != userNameUser) {
                model.addAttribute("msg", "用户名已存在！");
                return "register";
            }

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            user.setPassword(encoder.encode(user.getPassword()));
            user.setAccountNonLocked(true);
            user.setActive(false);
            WeiboRole weiboRole = new WeiboRole();
            if (user.getUsername().equals("admin")) {
                weiboRole.setRoleName("ROLE_ADMIN");
            } else {
                weiboRole.setRoleName("ROLE_USER");
            }
            roleRepository.save(weiboRole);
            List<WeiboRole> roles = new ArrayList<>();
            roles.add(weiboRole);
            user.setRoles(roles);
            userRepository.save(user);

            rabbitTemplate.convertAndSend("reg_email", user.getEmail());
            asyncSendEmailService.sendVerifyEmail(user.getEmail());
            //send email 已经异步执行,下面代码注释掉
        } catch (Exception e) {
            //logger.error("create user failed, ", e);
            e.printStackTrace();
            return "redirect:/error/401";
        }
        model.addAttribute("msg", "请到您的邮箱确认注册信息");
        return "register";
    }

    @GetMapping("/activeUserEmail")
    public String activeUserEmail(Model model, String email, String sid, RedirectAttributes redirectAttributes) {
        try {
            WeiboUser user = userRepository.findByEmail(email);
            Timestamp outDate = Timestamp.valueOf(user.getEmailOutDate());
            if (outDate.getTime() <= System.currentTimeMillis()) { //表示已经过期
                model.addAttribute("states", "签名过期");
                return "activeUserEmail";
//                System.out.print("过期");
            }
            String key = user.getEmail() + "$" + outDate.getTime() / 1000 * 1000 + "$" + user.getValidCode();//数字签名
            String digitalSignature = MD5Util.encode(key);
            if (digitalSignature.equals(sid)) {
                //return result(ExceptionMsg.LinkOutdated);
                userRepository.setActive(true, user.getEmail());
            } else {
                model.addAttribute("states", "签名错误");
                return "activeUserEmail";
            }
//            userRepository.
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error/401";
        }
        redirectAttributes.addFlashAttribute("alertMsg", "注册成功");
        return "redirect:/login";
    }

    @GetMapping("/activeUserEmail/reSend")
    public String activeUserEmail(Model model) {
        model.addAttribute("msg", "注册");
        return "register";
    }

}
