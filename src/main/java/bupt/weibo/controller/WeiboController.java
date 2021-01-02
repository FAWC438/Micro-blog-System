package bupt.weibo.controller;

import bupt.weibo.entity.Comment;
import bupt.weibo.entity.Weibo;
import bupt.weibo.repository.CommentRepository;
import bupt.weibo.repository.WeiboRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Controller
public class WeiboController {
    private CommentRepository commentRepository;
    private WeiboRepository weiboRepository;

    @Autowired
    public void setCommentRepository(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Autowired
    public void setWeiboRepository(WeiboRepository weiboRepository) {
        this.weiboRepository = weiboRepository;
    }

    @PostMapping("/release")
    public String releaseWeibo(String weiboText) {
        Weibo weibo = new Weibo();
        weibo.setWeiboText(weiboText);
        weibo.setReleaseTime(LocalDateTime.now());
        weiboRepository.save(weibo);
        System.out.println("---------release-----------");
//        return "redirect:/mainPage";
        return "redirect:/mainPage";
    }

    @RequestMapping("/mainPage")
    public String showMain(Model model,
                           @RequestParam(value = "start", defaultValue = "0") Integer start,
                           @RequestParam(value = "limit", defaultValue = "3") Integer limit,
                           @RequestParam(value = "type", defaultValue = "0") Integer type) {
        start = start < 0 ? 0 : start;
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(start, limit, sort);
        Page<Weibo> weibos;
        if(0 == type)
            weibos = weiboRepository.findByOrderByReleaseTimeDesc(pageable);
        else if (1 == type)
            weibos = weiboRepository.findByOrderByCommentNumDesc(pageable);
        else
            weibos = weiboRepository.findByOrderByLikeNumDesc(pageable);

        model.addAttribute("weibos", weibos);
        System.out.println("---------mainPage-----------");
        return "mainPage";
    }

}
