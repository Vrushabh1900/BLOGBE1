package newblogproject.example.newproject.service;

import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
@Service
public class EmailService {
    @Autowired
    JavaMailSender javaMailSender;
    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

//    @Value("${twilio.account.sid}")
//    private String twilioSid;
//
//    @Value("${twilio.auth.token}")
//    private String twilioAuthToken;
//
//    @Value("${twilio.phone.number}")
//    private String twilioPhoneNumber;
//
//    private boolean isTwilioInitialized = false;
//
//    private void initTwilio() {
//        if (!isTwilioInitialized) {
//            Twilio.init(twilioSid, twilioAuthToken);
//            isTwilioInitialized = true;
//        }
//    }

    public void sendWelcomeEmail(String toEmail, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Welcome to Our Platform");
        message.setText("Hello "+name+",\n\nThanks for registering with us nigger!\n\nRegards,\nAuthify Team");
        javaMailSender.send(message);
    }

    public void sendResetOtpEmail(String email, String otp) {
        SimpleMailMessage smp=new SimpleMailMessage();
        smp.setFrom(fromEmail);
        smp.setTo(email);
        smp.setSubject("Password reset otp");
        smp.setText("Your OTP for resetting your password is "+otp+". Use this OTP to proceed with resetting your password.");
        javaMailSender.send(smp);
    }

//    public void sendPhoneVerificationOtp(String phoneNumber, String otp) {
//        initTwilio();
//        if (!phoneNumber.startsWith("+")) {
//            phoneNumber = "+91" +phoneNumber; // for India
//        }
//
//        try {
//            Message message = Message.creator(
//                    new PhoneNumber(phoneNumber),
//                    new PhoneNumber(twilioPhoneNumber),
//                    "Your verification OTP is: " + otp
//            ).create();
//
//            if (message.getErrorCode() != null) {
//                throw new RuntimeException("Twilio error: " + message.getErrorMessage());
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to send OTP to phone: " + e.getMessage());
//        }
//    }

    public void sendEmailVerificationOtp(String email, String otp) {
        SimpleMailMessage smp=new SimpleMailMessage();
        smp.setFrom(fromEmail);
        smp.setTo(email);
        smp.setSubject("Verification otp");
        smp.setText("Your OTP for resetting your password is "+otp+". Use this OTP to proceed with resetting your password.");
        javaMailSender.send(smp);

    }
}
