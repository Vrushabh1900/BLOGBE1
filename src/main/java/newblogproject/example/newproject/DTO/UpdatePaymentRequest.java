package newblogproject.example.newproject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePaymentRequest {
    private String razorpayOrderId;
    private String paymentId;
    private String status;

}