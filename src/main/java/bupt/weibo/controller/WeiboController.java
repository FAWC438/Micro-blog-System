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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @ResponseBody
    @GetMapping("/likeAdd")
    public String likeAdd(@RequestParam("weibo_id") Long weibo_id) {
        Optional<Weibo> weibo = weiboRepository.findById(weibo_id);
        if (weibo.isPresent()) {
            Weibo weibo1 = weibo.get();
            Integer likeNum = weibo1.getLikeNum() + 1;
            weibo1.setLikeNum(likeNum);
            weiboRepository.save(weibo1);
            return likeNum.toString();
        } else {
            return "failure";
        }
    }

    @ResponseBody
    @GetMapping("/commentAdd")
    public String commentAdd(@RequestParam("weibo_id") Long id,
                             @RequestParam("commentText") String commentText,
                             HttpSession session) {
        Comment comment = new Comment();
        comment.setCommentText(commentText);

        SecurityContextImpl securityContext = (SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT");
        String name = ((UserDetails) securityContext.getAuthentication().getPrincipal()).getUsername();
        WeiboUser user = userRepository.findByUsername(name);
        comment.setWeiboUser(user);

        Weibo weibo = weiboRepository.findById(id).get();
        Integer commentNum = weibo.getCommentNum() + 1;
        weibo.setCommentNum(commentNum);

        comment.setWeibo(weibo);
        commentRepository.save(comment);

        return commentNum.toString();
    }

    @ResponseBody
    @GetMapping("/commentShow")
    String commentList(@RequestParam("weibo_id") Long weibo_id) {
        List<Comment> comments = commentRepository.findByWeiboId(weibo_id);
        StringBuilder commentJson = new StringBuilder("{");
        for (int i = 0; i < comments.size(); i++) {
            commentJson.append("\"").append(i).append("\":{\"name\":\"").append(comments.get(i).getWeiboUser().getUsername()).append("\",\"content\":\"").append(comments.get(i).getCommentText()).append("\"},");
        }
        commentJson = new StringBuilder(commentJson.substring(0, commentJson.length() - 1));
        commentJson.append("}");
//        System.out.println(comments.size());
//        System.out.println(comments);
        return commentJson.toString();
    }

    @PostMapping("/patternAt")
    @ResponseBody
    public String patternAt(String content) {
        String reg = "@[0-9_a-z\u4e00-\u9fa5]+";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(content);
        String result = content;
//        System.out.println(authorName);
//        WeiboUser author = userRepository.findByUsername(authorName);

        while (matcher.find()) {
            String tempStr = matcher.group(0);
            String userName = tempStr.substring(1); // 取得被@ 的用户名
            WeiboUser user = userRepository.findByUsername(userName);
            if (user != null) {
                String aLabel = "<a href=\"/myweibo?id=" + user.getId() + "\" style=\"color:blue;\" ><u>" + tempStr + "</u></a>";
                result = result.replaceAll(tempStr, aLabel);
            }
        }
        return result;
    }

    @PostMapping("/release")
    public String releaseWeibo(HttpServletRequest request, HttpSession session) {

        SecurityContextImpl securityContext = (SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT");
        String name = ((UserDetails) securityContext.getAuthentication().getPrincipal()).getUsername();

        MultipartHttpServletRequest params = ((MultipartHttpServletRequest) request);
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("upload-img");
        String weiboText = params.getParameter("weiboText");
        String atTarget = params.getParameter("at-targets");
        System.out.println("@info: " + atTarget);

        Weibo weibo = new Weibo();
        List<WeiboUser> list = new ArrayList<>();
        if (atTarget != null && !atTarget.isEmpty()) {
            String[] tempArr = atTarget.trim().split("\\s");
            for (String str : tempArr) {
                if (str != null && !str.isEmpty()) {
                    String temp = str.substring(1).replaceAll("\\u00A0+", "").trim();
                    WeiboUser searchRes = userRepository.findByUsername(temp);
                    if (searchRes != null) {
                        list.add(searchRes);
                    }
                }
            }
        }
        list = list.stream().distinct().collect(Collectors.toList());

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

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        weibo.setReleaseTime(df.format(new Date()));

        weiboRepository.save(weibo);
        user.getWeibos().add(weibo);
        userRepository.save(user);

//        if (!list.isEmpty()) {
//            for (WeiboUser u : list) {
//                System.out.println(u);
////                weibo.getAtUsers().add(u);
//                u.getAtMeWeibo().add(weibo);
//                userRepository.save(u);
//            }
//        }

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
