package newblogproject.example.newproject.repo;

import newblogproject.example.newproject.DTO.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RazorPayRepo extends JpaRepository<PaymentOrder,Long> {
    PaymentOrder findByRazorpayOrderId(String razorpayOrderId);
}
