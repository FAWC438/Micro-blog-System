package bupt.weibo.repository;

import bupt.weibo.entity.WeiboUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface UserRepository extends JpaRepository<WeiboUser, Long>, JpaSpecificationExecutor<WeiboUser> {
    WeiboUser findById(long id);

    WeiboUser findByUsername(String name);

}
