package newblogproject.example.newproject.service;


import newblogproject.example.newproject.Events.BlogCreatedEvent;
import newblogproject.example.newproject.models.Blog;
import newblogproject.example.newproject.models.Comment;
import newblogproject.example.newproject.models.Users;
import newblogproject.example.newproject.repo.BlogRepo;
import newblogproject.example.newproject.repo.UserRepo;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
public class BlogService {
    @Autowired
BlogRepo Blogrepo;
    @Autowired
    UserRepo userRepo;
    @Autowired
    TransactionTemplate tt;
    @Autowired
    ApplicationEventPublisher publisher;

@Transactional(isolation = Isolation.READ_COMMITTED)
    public Blog createpost(Blog blogo, MultipartFile imageFile,String email) throws IOException {

        publisher.publishEvent(new BlogCreatedEvent(blogo,imageFile));

        blogo.setUseremailid(email);
        blogo.setImageData(imageFile.getBytes());
        blogo.setImageType(imageFile.getContentType());
        blogo.setImageName(imageFile.getOriginalFilename());
        return Blogrepo.save(blogo);
    }

    @Transactional( isolation=Isolation.READ_COMMITTED)
    @Cacheable(value = "allblogs")
    public List<Blog> getposts() {
        List<Blog> allblogs= Blogrepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
    return allblogs;
    }


    public List<Blog> getpostbyuser(String email)
    {
        return Blogrepo.findByUseremailid(email).orElseThrow(()->new UsernameNotFoundException("email not found"));
    }

@Transactional(propagation = Propagation.REQUIRES_NEW, isolation=Isolation.REPEATABLE_READ)
public Blog updatepostbyid(int editingId,Blog blogo,MultipartFile imageFile) throws IOException {
//      Blog blog1=repo.findById(editingId).orElse(null);
    blogo.setImageData(imageFile.getBytes());
    blogo.setImageType(imageFile.getContentType());
    blogo.setImageName(imageFile.getOriginalFilename());
    return Blogrepo.save(blogo);
}



    public void deletepost(int id, Blog blogo) {
        Blogrepo.delete(blogo);
    }


public List<Blog> searchByKeyword(String keyword) {
    return Blogrepo.searchByKeyword("%" + keyword + "%");
}

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void likePost(String email, int blogId) {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Blog blog = Blogrepo.findById(blogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));

        Hibernate.initialize(user.getLikedBlogs()); // ensure loaded

        boolean alreadyLiked = user.getLikedBlogs().stream()
                .anyMatch(b -> b.getId()==(blogId));

        if (alreadyLiked) {
            System.out.println("already liked 409");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Blog already liked");

        }

        user.getLikedBlogs().add(blog);
        userRepo.save(user);
    }


//    @Transactional
    public void unlikePost(String email, int blogId) {
      tt.execute(status->  { Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Blog blog = Blogrepo.findById(blogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));

        Hibernate.initialize(user.getLikedBlogs());

        boolean wasLiked = user.getLikedBlogs().removeIf(b -> b.getId() == (blogId));

        if (!wasLiked) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blog was not liked by this user");
        }

        userRepo.save(user);
      return null;});

    }



    public int countLikes(int postId) {
        return userRepo.countByLikedBlogs_Id(postId);
    }


    public long viewedbycount(int postId, String email) {
        Blog post = Blogrepo.findById(postId)
                .orElseThrow(() -> new UsernameNotFoundException("Post not found"));
        return post.getViewedBy().size();
    }


    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void adduser(int blogId,String email) {

        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));

        Blog blog = Blogrepo.findById(blogId)
                .orElseThrow(() -> new UsernameNotFoundException("Blog post not found with id " + blogId));
        //Hibernate.initialize(user.getLikedBlogs());
//        if (!blog.getViewedBy().contains(user)) {
//            blog.getViewedBy().add(user);
//           repo.save(blog);
//        }
        boolean alreadyviewed=blog.getViewedBy().stream()
                .anyMatch(u->u.getEmail().equals(email));
        if(!alreadyviewed)
        {
            blog.getViewedBy().add(user);

        }
        else {
            throw new RuntimeException("user already viewed this blog");
        }
    }


}
