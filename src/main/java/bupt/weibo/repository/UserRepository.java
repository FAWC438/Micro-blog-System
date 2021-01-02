package bupt.weibo.repository;

import bupt.weibo.entity.WeiboUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<WeiboUser, Long>, JpaSpecificationExecutor<WeiboUser> {
    WeiboUser findById(long id);

    WeiboUser findByUsername(String name);

}
