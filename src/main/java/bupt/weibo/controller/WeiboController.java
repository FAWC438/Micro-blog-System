package bupt.weibo.controller;

import bupt.weibo.entity.Comment;
import bupt.weibo.entity.Picture;
import bupt.weibo.entity.Weibo;
import bupt.weibo.entity.WeiboUser;
import bupt.weibo.repository.CommentRepository;
import bupt.weibo.repository.PictureRepository;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
public class WeiboController {
    private CommentRepository commentRepository;
    private WeiboRepository weiboRepository;
    private UserRepository userRepository;
    private PictureRepository pictureRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setCommentRepository(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Autowired
    public void setWeiboRepository(WeiboRepository weiboRepository) {
        this.weiboRepository = weiboRepository;
    }

    @Autowired
    public void setPictureRepository(PictureRepository pictureRepository) {
        this.pictureRepository = pictureRepository;
    }

    @PostMapping("/release")
    public String releaseWeibo(HttpServletRequest request, HttpSession session, RedirectAttributes redirectAttributes) {

        SecurityContextImpl securityContext = (SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT");
        String name = ((UserDetails) securityContext.getAuthentication().getPrincipal()).getUsername();

        MultipartHttpServletRequest params = ((MultipartHttpServletRequest) request);
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("upload-img");
        String weiboText = params.getParameter("weiboText");

        Weibo weibo = new Weibo();
        WeiboUser user = userRepository.findByUsername(name);
        if (!files.isEmpty()) {
            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();
                String type = fileName.indexOf(".") != -1 ? fileName.substring(fileName.lastIndexOf(".")) : null;
                if (!(".GIF".equalsIgnoreCase(type) || ".PNG".equalsIgnoreCase(type) || ".JPG".equalsIgnoreCase(type))) {
//                    redirectAttributes.addFlashAttribute("errorMsg", "图片格式错误");
//                    return "redirect:/mainPage";
                    continue;
                }
                fileName = UUID.randomUUID() + type;
                String filePath = "D:\\code\\WebFinal\\src\\main\\resources\\static\\picData\\" + fileName;
                System.out.println("File Path:" + filePath);
                if (!file.isEmpty()) {
                    try {
                        file.transferTo(new File(filePath));
                        Picture newPic = new Picture();
                        newPic.setPicturePath("/uploadPic/" + fileName);
                        System.out.println(fileName);
                        newPic.setWeiboUser(user);
                        weibo.getPictures().add(newPic);
                        pictureRepository.save(newPic);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "redirect:/error/400";
                    }
                } else {
                    return "redirect:/error/400";
                }
            }
        }

        weibo.setWeiboUser(user);
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
        if (0 == type)
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
