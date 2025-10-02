package newblogproject.example.newproject.DTO;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import newblogproject.example.newproject.models.Users;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String razorpayOrderId;
    private String receiptId;
    private String paymentId;
    private String status;
    private Integer amount;
    private String currency;


    @ManyToOne
    private Users paiduser;

}

