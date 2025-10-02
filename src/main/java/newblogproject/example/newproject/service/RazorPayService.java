package newblogproject.example.newproject.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import newblogproject.example.newproject.DTO.PaymentOrder;
import newblogproject.example.newproject.models.Users;
import newblogproject.example.newproject.repo.RazorPayRepo;
import newblogproject.example.newproject.repo.UserRepo;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class RazorPayService {

        @Autowired
        private RazorPayRepo paymentOrderRepository;
        @Autowired
    UserRepo userRepo;
    @Autowired
    private static final String WEBHOOK_SECRET = "myfirstwebhookusingrazorpaycro";

    @Value("${RZP_SECRET_KEY}")
String testkey;
        @Value("${RZP_TEST_KEY}")
    String keysecret;
    @Transactional
        public PaymentOrder createRazorpayOrder(int amount, String currency, String receiptId, String email) throws RazorpayException {

            Users user1=userRepo.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("email not found"+email));
            RazorpayClient razorpayClient = new RazorpayClient(testkey, keysecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount*100);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", receiptId);

            Order order = razorpayClient.orders.create(orderRequest);

            PaymentOrder paymentOrder = new PaymentOrder();
            paymentOrder.setRazorpayOrderId(order.get("id"));
            paymentOrder.setReceiptId(receiptId);
            paymentOrder.setAmount(amount);
            paymentOrder.setCurrency(currency);
            paymentOrder.setStatus("created");
            paymentOrder.setPaiduser(user1);

            paymentOrderRepository.save(paymentOrder);
            return paymentOrder;
        }

        @Transactional
        public void updatePaymentDetails(String razorpayOrderId, String paymentId, String status) {
            PaymentOrder po = paymentOrderRepository.findByRazorpayOrderId(razorpayOrderId);
            if(po != null) {
                po.setPaymentId(paymentId);
                po.setStatus(status);
                paymentOrderRepository.save(po);
            }
        }


    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            String computedSignature = hmacSHA256(payload, WEBHOOK_SECRET);
            return computedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String hmacSHA256(String data, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(hash);
    }

    public void processWebhookPayload(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            JSONObject paymentEntity = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");

            String paymentId = paymentEntity.getString("id");
            String orderId = paymentEntity.getString("order_id");
            String status = paymentEntity.getString("status");

            updatePaymentDetails(orderId, paymentId, status);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }




    }



