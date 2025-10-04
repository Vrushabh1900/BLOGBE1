package newblogproject.example.newproject.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import newblogproject.example.newproject.DTO.AuthRequest;
import newblogproject.example.newproject.DTO.ProfileRequest;
import newblogproject.example.newproject.DTO.ProfileResponse;
import newblogproject.example.newproject.DTO.ResetOtpRequest;
import newblogproject.example.newproject.models.Users;
import newblogproject.example.newproject.repo.UserRepo;
import newblogproject.example.newproject.service.JWTservice;
import newblogproject.example.newproject.service.MyUserDetailsService;
import newblogproject.example.newproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@RestController
@RequestMapping("/api")
@Slf4j
public class UserController {
    @Autowired
    UserService service;
    @Autowired
    UserRepo repo;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    BCryptPasswordEncoder SHA;
    @Autowired
    MyUserDetailsService myUserDetailsService;
    @Autowired
    JWTservice jwTservice;

    @PostMapping("/register")
    public ResponseEntity<?> registeruser(@Valid @RequestBody ProfileRequest profileRequest)
    {
try{
    return new ResponseEntity<>(service.createuser(profileRequest),HttpStatus.CREATED);
} catch (Exception e) {
    return new ResponseEntity<>(e.getMessage(),HttpStatus.CONFLICT);
}

    }
@PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public ResponseEntity<List<Users>> adminacess()
{
    try{
       return new ResponseEntity<>(service.getAllusers(),HttpStatus.OK);
    } catch (Exception e) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
    }
}

@PostMapping("/login")
public ResponseEntity<?> loginpage(@RequestBody AuthRequest authRequest)
{
    try {
        authenticate(authRequest.getEmail(),authRequest.getPassword());
        UserDetails userDetails=myUserDetailsService.loadUserByUsername(authRequest.getEmail());

        String JwtToken=jwTservice.generateToken(userDetails.getUsername());
        String refreshToken = jwTservice.generateRefreshToken(userDetails.getUsername());
        ResponseCookie RC=ResponseCookie.from("jwt",JwtToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("None")
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .path("/api/refresh")
                .maxAge(Duration.ofDays(7))
                .sameSite("None")
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, RC.toString(),refreshCookie.toString())
                .body("Jwttoken:"+JwtToken+"\nrefreshtoken:"+refreshToken);
    }

    catch(BadCredentialsException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", "Email or password is incorrect");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    } catch(DisabledException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", "Account is disabled");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }catch(Exception ex) {
        ex.printStackTrace(); // Add this
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", "Authentication failed: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

}

    private void authenticate(String email,String password)
    {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,password));
    }

    @PostMapping("/send-reset-otp")
    public ResponseEntity<?> sendresetotp(@CurrentSecurityContext(expression ="authentication?.name")String email)
    {
        try
        {
           service.sendresetotp(email);
            return new ResponseEntity<>("email is sent",HttpStatus.OK);
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
        }
    }
    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetOtpRequest resetOtpRequest)
    {
        try
        {
           service.resetPassword(resetOtpRequest.getEmail(),resetOtpRequest.getOtp(),resetOtpRequest.getNpassword());
            return new ResponseEntity<>("Password was rest",HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
        }
    }

//    @PostMapping("/2fa/send")
//    public ResponseEntity<?> send2FAOtp(@RequestBody Map<String, String> request,@CurrentSecurityContext(expression = "authentication?.name")String email) {
//        String emailOrPhone = request.get("contact"); // take from authenticated user
//        String method = request.get("method"); // "EMAIL" or "PHONE"
//
//        try {
//            service.sendOtp(method, emailOrPhone,email);
//            return ResponseEntity.ok("OTP sent via " + method);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }
    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request,@CurrentSecurityContext(expression = "authentication?.name")String email) {
        String contact = request.get("contact");
        String method = request.get("method"); // "EMAIL" or "PHONE"
        String otp = request.get("otp");

        try {
            boolean valid = service.verifyOtp(method, contact, otp,email);
            if (valid) {
                return ResponseEntity.ok("OTP verified");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired OTP");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refresh_token", required = false) String refreshToken) {
        if (refreshToken == null || jwTservice.isTokenExpired(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired or missing");
        }
        String email = jwTservice.extractEmail(refreshToken);
        UserDetails user= myUserDetailsService.loadUserByUsername(email);
        if (!jwTservice.validateToken(refreshToken, user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        String JwtToken=jwTservice.generateToken(email);
        String newRefreshToken = jwTservice.generateRefreshToken(email);

        ResponseCookie jwt = ResponseCookie.from("jwt", JwtToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMinutes(5))
                .sameSite("None")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .path("/api/refresh")
                .maxAge(Duration.ofDays(7))
                .sameSite("None")

                .build();

        log.info("---------------refresh token received:{}-------------------",refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwt.toString(), refreshCookie.toString())
                .body(Map.of("message", "Tokens refreshed"));
    }
@GetMapping("/users")
public ResponseEntity<ProfileResponse> getusers(@CurrentSecurityContext(expression = "authentication?.name")String email)
{
    return new ResponseEntity<>(service.findByemail(email),HttpStatus.OK);
}

//@PostMapping("/viewpost/{id}")
//    public void viewpost(@PathVariable int blogid,@CurrentSecurityContext(expression = "authentication?.name")String email)
//{
//    try{
//        bs
//    }
//}
}






