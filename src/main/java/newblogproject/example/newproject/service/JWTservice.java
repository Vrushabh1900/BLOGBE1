package newblogproject.example.newproject.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import newblogproject.example.newproject.models.Users;
import newblogproject.example.newproject.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.function.Function;

@Service
public class JWTservice {

    private final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7;
    @Autowired
    UserRepo userRepo;

//private String secretkey="bf19fbed1d9d8678478e185aee3c91e20f2aef714a7b9fa47955e72c4d64964d";
//private String secretkey = "3uLuRQ2xkKZbCzAXooyLr04JQADq3m+2ZqK9VYlGZ2g=";
  private String secretkey=  "cfJVvt0sJDZSLxKTZHgN7qzRCU7ZCy9tK1WmFEuH8dg=";


//
//    public JWTservice() {
//        try {
//            KeyGenerator KG = KeyGenerator.getInstance("HmacSHA256");
//            KG.init(256);
//            SecretKey sk = KG.generateKey();
////secretkey = Base64.getEncoder().encodeToString(sk.getEncoded());
//            secretkey = Encoders.BASE64.encode(sk.getEncoded());
//            System.out.println(secretkey);
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//
//    }

    public String generateToken(String email) {
        Map<String,Object> claims=new HashMap<>();
        Users user= userRepo.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("user not found"+email));
        claims.put("roles", user.getRoles());
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration((new Date(System.currentTimeMillis()+1000*60*15)))
                .and()
                .signWith(getKeyy())
                .compact();

    }


    private SecretKey getKeyy()
    {
        byte[] skey= Base64.getDecoder().decode(secretkey);
        return Keys.hmacShaKeyFor(skey);
    }


    public String extractEmail(String token) {
        // extract the username from jwt token
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKeyy())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractEmail(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

  public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    public String generateRefreshToken(String email) {
        Map<String,Object> claims=new HashMap<>();
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration((new Date(System.currentTimeMillis()+1000*60*7)))
                .and()
                .signWith(getKeyy())
                .compact();
    }

}
