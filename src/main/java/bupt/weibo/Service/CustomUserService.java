package bupt.weibo.Service;

import bupt.weibo.entity.WeiboUser;
import bupt.weibo.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Data
public class CustomUserService implements UserDetailsService {
    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        WeiboUser user = userRepository.findByUsername(s);
        if (user == null) {
            WeiboUser emailUser = userRepository.findByEmail(s);
            if (emailUser == null) {
                throw new UsernameNotFoundException("用户名、邮箱不存在!");
            } else {
                user = userRepository.findByEmail(s);
            }
        }
        return user;
    }
}
