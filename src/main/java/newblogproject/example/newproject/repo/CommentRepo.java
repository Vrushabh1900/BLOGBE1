package newblogproject.example.newproject.repo;


import newblogproject.example.newproject.models.Blog;
import newblogproject.example.newproject.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepo extends JpaRepository<Comment,Long> {
    @Override
    public Optional<Comment> findById(Long ID);

    List<Comment> findByBlogscomments(Blog blog);

    @Query("SELECT SIZE(c.likedby) FROM Comment c WHERE c.id = :id")
  Long countLikesByCommentId(@Param("id") Long commentId);

}
