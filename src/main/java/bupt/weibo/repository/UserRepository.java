package bupt.weibo.repository;

import bupt.weibo.entity.WeiboUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<WeiboUser, Long>, JpaSpecificationExecutor<WeiboUser> {
    WeiboUser findById(long id);

    WeiboUser findByUsername(String name);

    WeiboUser findByEmail(String email);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update WeiboUser  set emailOutDate=:emailOutDate, validCode=:validCode where email=:email")
    void setEmailOutDateAndValidCode(@Param("emailOutDate") String emailOutDate, @Param("validCode") String validCode, @Param("email") String email);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update WeiboUser set active=:active where email=:email")
    void setActive(@Param("active") Boolean active, @Param("email") String email);
}
