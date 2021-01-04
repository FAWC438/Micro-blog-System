package bupt.weibo.repository;

import bupt.weibo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByWeiboId(Long weibo_id);
}
