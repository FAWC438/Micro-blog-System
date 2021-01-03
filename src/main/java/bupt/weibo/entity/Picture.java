package bupt.weibo.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Picture {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String picturePath;

    @OneToOne
    private WeiboUser weiboUser;
}
