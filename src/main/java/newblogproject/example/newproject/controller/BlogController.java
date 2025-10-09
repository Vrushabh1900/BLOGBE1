package newblogproject.example.newproject.controller;

import com.twilio.twiml.voice.Application;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import newblogproject.example.newproject.DTO.SliceResponse;
import newblogproject.example.newproject.Events.BlogCreatedEvent;
import newblogproject.example.newproject.models.Blog;
import newblogproject.example.newproject.models.Comment;
import newblogproject.example.newproject.repo.BlogRepo;
import newblogproject.example.newproject.service.BlogService;

import newblogproject.example.newproject.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class BlogController {
    @Autowired
    BlogRepo repo;
@Autowired
    BlogService bs;
@Autowired
    CommentService cs;

    @GetMapping("/")
     public String first(HttpServletRequest req)
{
    return "hello"+ req.getSession().getId();
}


    @PostMapping("/posts")
    public ResponseEntity<?> createblogs(@RequestPart Blog blogo, @RequestPart MultipartFile imagefile, @CurrentSecurityContext(expression = "authentication?.name")String email)
    {
        try
        {
                bs.createpost(blogo,imagefile,email);

return new ResponseEntity<>("BLOG CREATED", HttpStatus.OK);
        }
        catch(Exception e)
        {
return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/posts")
    public Page<Blog> getBlogs(
            @RequestParam(defaultValue = "0") int page,                         // Page number
            @RequestParam(defaultValue = "10") int size,                        // Blogs per page
            @RequestParam(defaultValue = "id") String sortBy,           // Sort field
            @RequestParam(defaultValue = "desc") String sortDir                // Sort direction
    ) {
        List<String> sortfield = List.of("id", "title", "createdAt");
        if (!sortfield.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return repo.findAll(pageable);
    }

    @GetMapping("/posts/slice")
    public SliceResponse<Blog> getBlogsSlice(
            @RequestParam Optional<String> keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        System.out.println(" HIT /posts/slice ");
        Pageable pageable = PageRequest.of(page, size);
//        Slice<Blog> slice = repo.findByKeyword(keyword.orElse(""), pageable);
        Slice<Blog> slice;
        if (keyword.isPresent() && !keyword.get().isBlank()) {
            slice = repo.findByKeyword(keyword.get(), pageable);
        } else {
            slice = repo.findAll(pageable);
        }

        return new SliceResponse<>(
                slice.getContent(),
                slice.getNumber(),
                slice.getSize(),
                slice.hasNext()
        );
    }

    @GetMapping("/posts/getposts")
    public List<Blog> getposts(){
        return bs.getposts();
    }

    @GetMapping("/posts/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable int id) {
        Blog blog = repo.findById(id).orElse(null);
        if (blog == null || blog.getImageData() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.valueOf(blog.getImageType())); // e.g., "image/jpeg"
//        return new ResponseEntity<>(blog.getImageData(), headers, HttpStatus.OK);

//byte[] nigger= blog.getImageData();
//        return ResponseEntity.ok()
//                .contentType(MediaType.valueOf(blog.getImageType()))
//                .body(blog.getImageData());

        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.valueOf(blog.getImageType()));
        return new ResponseEntity<>(blog.getImageData(),headers,HttpStatus.OK);

    }

    @GetMapping("/user/getblogs")
    public ResponseEntity<?> getblogsbyuser(@CurrentSecurityContext(expression = "authentication?.name")String email)
    {
 try{
     if(bs.getpostbyuser(email).isEmpty())
     {
         return new ResponseEntity<>("No posts have been created !",HttpStatus.OK);
     }
     else
         return new ResponseEntity<>(bs.getpostbyuser(email),HttpStatus.OK);

 } catch (Exception e) {
     throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Email not found");
 }


    }
    @PostMapping("/posts/{editingId}")
    public ResponseEntity<?> updateblog(@PathVariable int editingId,@RequestPart Blog blogo, @RequestPart MultipartFile imagefile)
    { Blog blog1=null;
        blog1=repo.findById(editingId).orElse(null);
          try
            { blog1=bs.updatepostbyid(editingId,blogo,imagefile);
        return new ResponseEntity<>("Updated", HttpStatus.OK);}
catch(OptimisticLockException e)
            {
                Blog LatestBlog= repo.findById(editingId).orElseThrow(()-> new UsernameNotFoundException("Blog not found"));
                String errorMessage = "This blog post was updated by someone else while you were editing. " +
                        "Please review the latest version and re-apply your changes if needed.";
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "message", errorMessage,
                                "latestBlog", LatestBlog,
                                "yourAttemptedChanges", blogo
                        ));

    }
          catch (IOException e) {
              throw new RuntimeException(e);
          }

    }


        @DeleteMapping("/posts/{id}")
        public ResponseEntity<?> deleteposts(@PathVariable int id)
        {
Blog blogo=repo.findById(id).orElse(null);
            if(blogo!=null)
            { bs.deletepost(id,blogo);
                return new ResponseEntity<>("Deleted", HttpStatus.OK);}
            else
            {return new ResponseEntity<>("Not deleted you nigger",HttpStatus.BAD_REQUEST);
        }
}

   @GetMapping("/posts/search")
  public List<Blog> searchPosts(@RequestParam("keyword") String keyword) {
    return bs.searchByKeyword(keyword);
}



@PostMapping("/posts/{id}/like")
    public ResponseEntity<String> likeBlog(@PathVariable("id") int blogId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
try
{
    bs.likePost(userDetails.getUsername(), blogId);
    return ResponseEntity.ok("Blog liked successfully");
} catch (Exception e) {
    System.out.println(e.getMessage());
        throw new ResponseStatusException(HttpStatus.CONFLICT,e.getMessage());

}
    }

    @DeleteMapping("/posts/{id}/unlike")
    public ResponseEntity<String> unlikeBlog(@PathVariable("id") int blogId,
                                             @AuthenticationPrincipal UserDetails userDetails) {

        bs.unlikePost(userDetails.getUsername(), blogId);
        return ResponseEntity.ok("Blog unliked successfully");
    }


    @GetMapping("/posts/{postId}/likes")
    public int getLikes(@PathVariable int postId) {

    return bs.countLikes(postId);
    }

    @GetMapping("/posts/{blogid}/views")
    public Long getviews(@PathVariable int blogid,@CurrentSecurityContext(expression = "authentication?.name")String email)
    {
        try
        {return bs.viewedbycount(blogid,email);}
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"weird shi");
        }
    }

    @PostMapping("/posts/{blogid}/viewuser")
    public void postview(@PathVariable int blogid,  @AuthenticationPrincipal UserDetails userDetails)
    {
        try{bs.adduser(blogid,userDetails.getUsername());} catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }






}









