package newblogproject.example.newproject.controller;

import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import newblogproject.example.newproject.DTO.CreateOrderRequest;
import newblogproject.example.newproject.DTO.PaymentOrder;
import newblogproject.example.newproject.DTO.UpdatePaymentRequest;
import newblogproject.example.newproject.models.Users;
import newblogproject.example.newproject.service.RazorPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@Slf4j
public class PaymentController {
    @Autowired
    private RazorPayService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<PaymentOrder> createOrder(@RequestBody CreateOrderRequest request, @CurrentSecurityContext(expression = "authentication?.name") String email) throws RazorpayException {
        PaymentOrder order = null;
        try {
            order = paymentService.createRazorpayOrder(
                    request.getAmount(),
                    request.getCurrency(),
                     "order_" + System.currentTimeMillis(),
                    email
            );
            System.out.println(order);
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: ", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"payment creation failed");
        }
        return ResponseEntity.ok(order);
    }

    // This is for the frontend which sends the JSON response on payment completion generated using RazorPay Script
    @PostMapping("/update-payment")
    public ResponseEntity<?> updatePayment(@RequestBody UpdatePaymentRequest request) {
        try
        {paymentService.updatePaymentDetails(request.getRazorpayOrderId(), request.getPaymentId(), request.getStatus());} catch (
                Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"payment updation failed");
        }
        return ResponseEntity.ok("Payment updated");
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String razorpaySignature) {

        boolean isValid = paymentService.verifyWebhookSignature(payload, razorpaySignature);

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        paymentService.processWebhookPayload(payload);

        return ResponseEntity.ok("Webhook processed");
    }
}

