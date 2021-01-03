package bupt.weibo.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
public class Weibo {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String weiboText;

    @Column(nullable = false)
    private Integer commentNum = 0;

    @Column(nullable = false)
    private Integer likeNum = 0;

    @OneToOne
    private WeiboUser weiboUser;

    @OneToMany()
    private List<Comment> comments = new ArrayList<>();

    @OneToMany()
    private List<Picture> pictures = new ArrayList<>();

    //    @Column(nullable = false)
    private LocalDateTime releaseTime;
}
