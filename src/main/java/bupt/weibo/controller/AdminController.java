package bupt.weibo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class AdminController {
    @GetMapping("/admin")
    String admin() {
        return "redirect:/h2-console";
    }
}
