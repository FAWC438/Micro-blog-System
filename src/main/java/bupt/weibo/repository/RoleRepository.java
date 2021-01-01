package bupt.weibo.repository;

import bupt.weibo.entity.WeiboRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<WeiboRole, Long> {
}
