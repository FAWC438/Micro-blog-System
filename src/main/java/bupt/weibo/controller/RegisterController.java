package bupt.weibo.controller;

import bupt.weibo.entity.WeiboRole;
import bupt.weibo.entity.WeiboUser;
import bupt.weibo.repository.RoleRepository;
import bupt.weibo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/")
public class RegisterController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;


    @GetMapping("/register")
    String register() {
        return "register";
    }

    @PostMapping("/register")
    String checkRegister(Model model, String username, String password, RedirectAttributes redirectAttributes) {
        WeiboUser weiboUser = userRepository.findByUsername(username);
        if (weiboUser != null) {
            model.addAttribute("msg", "用户名已存在！");
            return "register";
        } else {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            weiboUser = new WeiboUser();
            weiboUser.setUsername(username);
            weiboUser.setPassword(passwordEncoder.encode(password));
            weiboUser.setAccountNonLocked(true);
            WeiboRole weiboRole = new WeiboRole();
            if (username.equals("admin")) {
                weiboRole.setName("ROLE_ADMIN");
            } else {
                weiboRole.setName("ROLE_USER");
            }
            roleRepository.save(weiboRole);
            List<WeiboRole> roles = new ArrayList<>();
            roles.add(weiboRole);
            weiboUser.setRoles(roles);
            userRepository.save(weiboUser);
            redirectAttributes.addFlashAttribute("alertMsg", "注册成功！");
            return "redirect:/login";
        }


    }

}
