package bupt.weibo.controller;

import bupt.weibo.entity.Weibo;
import bupt.weibo.entity.WeiboUser;
import bupt.weibo.repository.UserRepository;
import bupt.weibo.repository.WeiboRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

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


    @RequestMapping("/myweibo")
    public String myWeibo(HttpSession session, Model model,
                          @RequestParam(value = "start", defaultValue = "0") Integer start,
                          @RequestParam(value = "limit", defaultValue = "3") Integer limit) {
        start = start < 0 ? 0 : start;
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(start, limit, sort);
        Page<Weibo> weibos = weiboRepository.findByWeiboUserOrderByReleaseTimeDesc(getCurUser(session), pageable);
        System.out.println("-----------------myweibo----------------------");
        model.addAttribute("weibos", weibos);
        return "myweibo";
    }

    @RequestMapping("/myfans")
    public String myFans(HttpSession session, Model model) {
        System.out.println("-----------------fans----------------------");
        return "myfans";
    }


    @RequestMapping("/myat")
    public String myAt(HttpSession session, Model model) {
        System.out.println("-----------------myat----------------------");
        return "myat";
    }

}
