package newblogproject.example.newproject.service;

import newblogproject.example.newproject.models.Blog;
import newblogproject.example.newproject.models.Comment;
import newblogproject.example.newproject.models.Users;
import newblogproject.example.newproject.repo.BlogRepo;
import newblogproject.example.newproject.repo.CommentRepo;
import newblogproject.example.newproject.repo.UserRepo;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    BlogRepo Blogrepo;
    @Autowired
    UserRepo userRepo;
    @Autowired
    CommentRepo commentRepo;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void addcomment(int blogId, String email, Comment comment) {

        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));

        Blog blog = Blogrepo.findById(blogId)
                .orElseThrow(() -> new UsernameNotFoundException("Blog post not found with id " + blogId));


        comment.setAuthor(user);
        comment.setBlogscomments(blog);// not really needed
 commentRepo.save(comment);

    }

@Transactional
    public Comment likeComment(Long commentId, String email) {
        Comment comment= commentRepo.findById(commentId)
                .orElseThrow(()->new UsernameNotFoundException("Comment not found"));

        Hibernate.initialize(comment.getLikedby());

        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));

        if (comment.getLikedby().contains(user)) {
            throw new IllegalStateException("User has already liked this comment");
        }

        boolean alreadyliked= user.getLikedcomments().stream().anyMatch(b->b.getId().equals(commentId));
        if(alreadyliked)
        {
            System.out.println("already liked");
            throw new RuntimeException("comment already liked");
        }
        comment.getLikedby().add(user);


        return commentRepo.save(comment);
}


    @Transactional
    public Comment unlikeComment(Long commentId, String email) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new UsernameNotFoundException("Comment not found"));

        Hibernate.initialize(comment.getLikedby());

        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));

        if (!comment.getLikedby().contains(user)) {
            throw new IllegalStateException("User has not liked this comment yet");
        }

        comment.getLikedby().remove(user);
        return commentRepo.save(comment);
    }

    public void deleteComment(Long commentId) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        commentRepo.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPostId(int blogId) {

        Blog blog = Blogrepo.findById(blogId)
                .orElseThrow(() -> new UsernameNotFoundException("Blog post not found with id " + blogId));

        List<Comment> comments = commentRepo.findByBlogscomments(blog);

        comments.forEach(comment -> {
            Hibernate.initialize(comment.getLikedby());
            Hibernate.initialize(comment.getReplies());
        });

        return comments;
    }

    @Transactional
    public void addcommenttocomment(int blogid,Long pcommentid,Comment newcomment,String email) {

        Comment pcomment= commentRepo.findById(pcommentid).orElseThrow(()->new UsernameNotFoundException("Comment not found"));


        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));

        Blog blog = Blogrepo.findById(blogid)
                .orElseThrow(() -> new UsernameNotFoundException("Blog post not found with id " + blogid));


        newcomment.setAuthor(user);
        newcomment.setParentComment(pcomment);
        newcomment.setBlogscomments(blog);


        Hibernate.initialize(pcomment.getReplies());
        List<Comment> replies=pcomment.getReplies();
        replies.add(newcomment);

        commentRepo.save(newcomment);
    }

    public Long countLikes(Long commentid) {
        return commentRepo.countLikesByCommentId(commentid);
    }
}
