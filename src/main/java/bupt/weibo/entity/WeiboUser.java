package bupt.weibo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Data
public class WeiboUser implements UserDetails {

    // (strategy = GenerationType.IDENTITY)
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    @NotEmpty(message = "姓名不能为空")
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, unique = true)
    private String email;
    private String emailOutDate;
    private String validCode;
    private boolean AccountNonLocked;
    private boolean active;

    @ManyToMany(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    private List<WeiboRole> roles;

    @OneToMany
    private List<WeiboUser> weiboUsers = new ArrayList<>();

    @OneToMany
    private List<Weibo> weibos = new ArrayList<>();

    @Column(nullable = false)
    private Integer attentionNum = 0;

    @Column(nullable = false)
    private Integer fansNum = 0;

    @Column(nullable = false)
    private Integer weiboNum = 0;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> auths = new ArrayList<>();
        List<WeiboRole> roles = this.getRoles();
        for (WeiboRole role : roles) {
            auths.add(new SimpleGrantedAuthority(role.getRoleName()));
        }
        System.out.println(auths);
        return auths;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return AccountNonLocked;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
