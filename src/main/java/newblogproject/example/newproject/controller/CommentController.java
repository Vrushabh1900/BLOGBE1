package newblogproject.example.newproject.controller;

import lombok.RequiredArgsConstructor;
import newblogproject.example.newproject.models.Comment;
import newblogproject.example.newproject.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {


    private final CommentService commentService;

    @PostMapping("/{commentId}/like")
    public ResponseEntity<?> likeComment(
            @PathVariable Long commentId,
            @CurrentSecurityContext(expression = "authentication?.name")String email) {

        try
        { Comment updatedComment = commentService.likeComment(commentId, email);} catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"couldnt like"+e);
        }
        return ResponseEntity.ok("comment liked");
    }

    @DeleteMapping("/{commentId}/unlike")
    public ResponseEntity<?> unlikeComment(
            @PathVariable Long commentId,
            @CurrentSecurityContext(expression = "authentication?.name")String email) {
       try{ Comment updatedComment = commentService.unlikeComment(commentId, email);} catch (Exception e) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"couldnt unlike"+e);
       }
        return ResponseEntity.ok("comment unliked");
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        try{commentService.deleteComment(commentId);
            return new ResponseEntity<>("comment deleted",HttpStatus.OK);} catch (Exception e) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"oculdnt delte comment");
        }

    }


    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable int postId) {
        try{List<Comment> comments = commentService.getCommentsByPostId(postId);
          return new ResponseEntity<>(comments,HttpStatus.OK);} catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("/posts/{blogid}/comment")
    public ResponseEntity<?> commenttoblog(@PathVariable int blogid,@CurrentSecurityContext(expression = "authentication?.name")String email, @RequestBody Comment comment)
    {
        try{
            commentService.addcomment(blogid,email,comment);
            return new ResponseEntity<>("comment added",HttpStatus.OK);
            //pubsub event to send notif regarding liked comment can be added
        }
        catch (Exception e) {
            throw new ResponseStatusException( HttpStatus.BAD_REQUEST,"Comment not added "+ e);
        }
    }

    @PostMapping("/posts/{blogid}/comments/{pcommentid}")
  public ResponseEntity<?> commenttocomment(@PathVariable int blogid,@RequestBody Comment newcomment,@PathVariable Long pcommentid,@CurrentSecurityContext(expression = "authentication?.name")String email)
  {
      try{
          commentService.addcommenttocomment(blogid,pcommentid,newcomment,email);
          return new ResponseEntity<>("comment added",HttpStatus.OK);
          //pubsub event to send notif regarding liked comment can be added
      }
      catch (Exception e) {
          throw new ResponseStatusException( HttpStatus.BAD_REQUEST,"Comment not added");
      }
  }

  @GetMapping("/{commentid}")
  public Long getLikes(@PathVariable Long commentid) {
      return commentService.countLikes(commentid);
  }

}
