package newblogproject.example.newproject.repo;


import newblogproject.example.newproject.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<Users,Integer> {

    public Optional<Users> findByEmail(String username);
    public boolean existsByEmail(String email);
    public Optional<Users> findByPhonenumber(String phonenumber);

    @Query("SELECT COUNT(u) FROM Users u JOIN u.likedBlogs p WHERE p.id = :postId")
    int countByLikedBlogs_Id(@Param("postId") int postId);

}
