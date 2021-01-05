package bupt.weibo.controller;

import bupt.weibo.Service.AsyncSendEmailService;
import bupt.weibo.entity.Picture;
import bupt.weibo.entity.WeiboRole;
import bupt.weibo.entity.WeiboUser;
import bupt.weibo.repository.CommentRepository;
import bupt.weibo.repository.PictureRepository;
import bupt.weibo.repository.RoleRepository;
import bupt.weibo.repository.UserRepository;
import bupt.weibo.util.MD5Util;
import com.github.afkbrb.avatar.Avatar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/")
public class RegisterController {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private AsyncSendEmailService asyncSendEmailService;
    private AmqpTemplate rabbitTemplate;
    private PictureRepository pictureRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Autowired
    public void setAsyncSendEmailService(AsyncSendEmailService asyncSendEmailService) {
        this.asyncSendEmailService = asyncSendEmailService;
    }

    @Autowired
    public void setAmqpTemplate(AmqpTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Autowired
    public void setPictureRepository(PictureRepository pictureRepository) {
        this.pictureRepository = pictureRepository;
    }

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
    String registerByEmail(Model model, WeiboUser user, HttpServletRequest request) {
        try {

//            MultipartHttpServletRequest params = ((MultipartHttpServletRequest) request);
//            List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("upload-input");
            Avatar avatar = new Avatar();
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


            avatar.saveAsPNG("D:\\code\\WebFinal\\src\\main\\resources\\static\\picData\\avatar\\" + user.getUsername() + ".png");
            user.setAvatarPath("/uploadPic/avatar/" + user.getUsername() + ".png");

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
            asyncSendEmailService.sendVerifyEmail(user.getEmail(), "activeUserEmail");
            //send email 已经异步执行
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

    @GetMapping("/resetMailSend")
    @ResponseBody
    public String resetMailSend(HttpSession session) {
        SecurityContextImpl securityContext = (SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT");
        String name = ((UserDetails) securityContext.getAuthentication().getPrincipal()).getUsername();
        WeiboUser user = userRepository.findByUsername(name);
        try {
            rabbitTemplate.convertAndSend("reg_email", user.getEmail());
            asyncSendEmailService.sendVerifyEmail(user.getEmail(), "resetUserPassword");
        } catch (Exception e) {
            return "邮箱发送失败";
        }
        return "已发送重置密码邮箱";
    }

    @GetMapping("/resetUserPassword")
    public String resetUserPassword(Model model, String email, String sid) {
        try {
            WeiboUser user = userRepository.findByEmail(email);
            Timestamp outDate = Timestamp.valueOf(user.getEmailOutDate());
            if (outDate.getTime() <= System.currentTimeMillis()) { //表示已经过期
                model.addAttribute("states", "签名过期"); // 重新发送邮件？
                return "resetUserPassword";
//                System.out.print("过期");
            }
            String key = user.getEmail() + "$" + outDate.getTime() / 1000 * 1000 + "$" + user.getValidCode();//数字签名
            String digitalSignature = MD5Util.encode(key);
            if (digitalSignature.equals(sid)) {
                //return result(ExceptionMsg.LinkOutdated);
                model.addAttribute("states", "认证成功");
                model.addAttribute("email", email);
            } else {
                model.addAttribute("states", "签名错误");
            }
//            userRepository.
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error/401";
        }
        return "resetUserPassword";
    }

    @PostMapping("/resetUserPassword")
    public String judgeNewPassword(String newPassword, String email, RedirectAttributes redirectAttributes) {
        WeiboUser user = userRepository.findByEmail(email);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("alertMsg", "密码重置成功！");
        return "redirect:/login";
    }
}
