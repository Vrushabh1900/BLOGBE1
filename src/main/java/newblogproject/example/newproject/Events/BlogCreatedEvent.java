package newblogproject.example.newproject.Events;

import lombok.AllArgsConstructor;
import lombok.Data;
import newblogproject.example.newproject.models.Blog;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor

public class BlogCreatedEvent {
    private final Blog blogo;
    private final MultipartFile imageFile;
}
