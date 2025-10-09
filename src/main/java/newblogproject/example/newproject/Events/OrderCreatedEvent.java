package newblogproject.example.newproject.Events;

import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

@Data
@AllArgsConstructor
@Getter
@Setter
public class OrderCreatedEvent {
    private final String razorpayOrderId;
    private final String paymentId;
    private final String status;
}
