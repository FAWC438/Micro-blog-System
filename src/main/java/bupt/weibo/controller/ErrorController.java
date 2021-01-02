package bupt.weibo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorController {

    @GetMapping("/")
    String errorGet() {
        return "404";
    }

    @PostMapping("/")
    String errorPost() {
        return "404";
    }

    @GetMapping("/403")
    String error403() {
        return "403";
    }

    @GetMapping("/401")
    String error401() {
        return "401";
    }
}
