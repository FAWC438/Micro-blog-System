package bupt.weibo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
    @GetMapping("/admin")
    String admin() {
        return "redirect:/h2-console";
    }
}
