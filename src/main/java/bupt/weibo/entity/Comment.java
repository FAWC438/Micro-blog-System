package bupt.weibo.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Comment {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String commentText;

    @ManyToOne()
//    @JoinColumn(name = "weibo_user_id")
    private WeiboUser weiboUser;

    @ManyToOne()
    @JoinColumn(name = "weibo_id")
    private Weibo weibo;
}
