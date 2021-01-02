package bupt.weibo.controller;

import bupt.weibo.entity.WeiboUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController {

    @RequestMapping("/myweibo")
    public String myWeibo(WeiboUser user, Model model) {
        model.addAttribute("user", user);
        System.out.println("-----------------myweibo----------------------");
        return "myweibo";
    }

    @RequestMapping("/myfans")
    public String myFans(WeiboUser user, Model model) {
        model.addAttribute("user", user);
        System.out.println("-----------------fans----------------------");
        return "myfans";
    }


    @RequestMapping("/myat")
    public String myAt(WeiboUser user, Model model) {
        model.addAttribute("user", user);
        System.out.println("-----------------myat----------------------");
        return "myat";
    }

}
