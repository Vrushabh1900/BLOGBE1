package newblogproject.example.newproject.DTO;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
public class ProfileResponse implements Serializable {
    private String username;
    private String email;
    private Boolean isAccountVerified;
}
