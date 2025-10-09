package newblogproject.example.newproject.Listeners;

import newblogproject.example.newproject.Events.OrderCreatedEvent;
import newblogproject.example.newproject.service.RazorPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;

public class PaymentUpdateListener {
@Autowired
RazorPayService service;

    @EventListener
    @Order(1)
    @Async
    public void onOrderCreatedEvent(OrderCreatedEvent event) {
        service.updatePaymentDetails(event.getRazorpayOrderId(), event.getPaymentId(), event.getStatus());
    }
}
