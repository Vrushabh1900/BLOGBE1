package newblogproject.example.newproject.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetOtpRequest {
    @NotBlank(message="nigga what")
    private String npassword;
    @NotBlank(message = "OTP is required")
    private String otp;
    @NotBlank(message = "Email is required")
    private String email;
}