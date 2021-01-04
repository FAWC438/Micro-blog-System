package bupt.weibo.controller;

import bupt.weibo.entity.WeiboUser;
import bupt.weibo.repository.UserRepository;
import bupt.weibo.repository.WeiboRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class UserController {
    private UserRepository userRepository;
    private WeiboRepository weiboRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setWeiboRepository(WeiboRepository weiboRepository) {
        this.weiboRepository = weiboRepository;
    }

    public WeiboUser getCurUser(HttpSession session) {
        SecurityContextImpl securityContext = (SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT");
        String name = ((UserDetails) securityContext.getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByUsername(name);
    }

    @RequestMapping("/myWeibo")
    public String myWeibo(Model model, HttpSession session,
                          @RequestParam("id") Long id) { // id 为用户 id
        Long curId = getCurUser(session).getId();

        model.addAttribute("curId", curId);
        model.addAttribute("id", id);

        WeiboUser weiboUser = userRepository.findById(id).get();
        model.addAttribute("user", weiboUser);
        return "myWeibo";
    }

    @RequestMapping("/selfWeibo")
    public String myWeiboSelf(Model model, HttpSession session) {
        WeiboUser weiboUser = getCurUser(session);
        Long curId = weiboUser.getId();
        model.addAttribute("curId", curId);
        model.addAttribute("id", curId);
        model.addAttribute("user", weiboUser);
        return "myWeibo";
    }

    @RequestMapping("/fans")
    public String myFans(HttpSession session, Model model) {
        WeiboUser curUser = getCurUser(session);
        model.addAttribute("user", curUser);
        return "fans";
    }


    @RequestMapping("/at")
    public String myAt(HttpSession session, Model model) {
        WeiboUser curUser = getCurUser(session);
        model.addAttribute("user", curUser);
        return "at";
    }

    @ResponseBody
    @GetMapping("/checkAttention")
    public String checkAttention(@RequestParam("curId") Long curId,
                                 @RequestParam("id") Long id) { // curId 为登录用户
        System.out.println(curId);
        System.out.println(id);

        Optional<WeiboUser> curUserOp = userRepository.findById(curId);
        WeiboUser curUser;
        if (curUserOp.isPresent()) {
            curUser = curUserOp.get();
        } else {
            return "error";
        }

        Optional<WeiboUser> userOp = userRepository.findById(id);
        WeiboUser user;
        if (userOp.isPresent()) {
            user = userOp.get();
        } else {
            return "Error";
        }

        if (curUser.getAttentionUser().contains(user) && user.getFansUser().contains(curUser)) {
            // 取消关注
            curUser.getAttentionUser().remove(user);
            user.getFansUser().remove(curUser);
            userRepository.save(user);
            userRepository.save(curUser);
            return "False";
        } else if (!curUser.getAttentionUser().contains(user) && !user.getFansUser().contains(curUser)) {
            // 开始关注
            curUser.getAttentionUser().add(user);
            user.getFansUser().add(curUser);
            userRepository.save(user);
            userRepository.save(curUser);
            return "True";
        } else {
            return "Error";
        }
    }
}
