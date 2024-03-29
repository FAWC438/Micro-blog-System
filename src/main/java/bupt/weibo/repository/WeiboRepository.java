package bupt.weibo.repository;

import bupt.weibo.entity.Weibo;
import bupt.weibo.entity.WeiboUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeiboRepository extends JpaRepository<Weibo, Long> {
    Page<Weibo> findByOrderByReleaseTimeDesc(Pageable pageable);

    Page<Weibo> findByOrderByCommentNumDesc(Pageable pageable);

    Page<Weibo> findByOrderByLikeNumDesc(Pageable pageable);

    Page<Weibo> findByWeiboUserOrderByReleaseTimeDesc(WeiboUser user, Pageable pageable);

}
