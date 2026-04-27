package com.Kee.V2C.service.Authentication;

import com.Kee.V2C.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /*generates a signInKey based on the secret key*/
    public SecretKey signInKey(){
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String generateToken(UserDetailsImpl userDetails){
            return Jwts.builder() //we build JWT token using builder
                    .subject(userDetails.getUsername())
                    .claim("userId",userDetails.getId())
                    .claim("roles", userDetails.getAuthorities())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis()+jwtExpiration))
                    .signWith(signInKey())
                    .compact();
    }

    public String extractUserName(String token){
        try {
            return extractClaim(token, Claims::getSubject);
        }
        catch (JwtException | IllegalArgumentException e){
            return null;
        }
    }

    public Date extractExpiration(String token){
        return extractClaim(token,Claims::getExpiration);
    }


    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver){
        //Function is a functional interface from java.util.function
        //it takes an input Claims and produces output T, according to the logic of the function
        final Claims claims=extractAllClaims(token);
        return claimsResolver.apply(claims);//apply the logic of the function and return output
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()//we parse JWT token using the parser
                .verifyWith(signInKey())//use the sign key for verification
                .build()
                .parseSignedClaims(token)//extracts all the claims
                .getPayload();//maps JSON body of jwt into a claim map
    }

    //we need two validations , isTokenValid and isTokenExpired

    public boolean isTokenValid(String token,UserDetails userDetails){
        String tokenUserName=extractUserName(token);
        return (tokenUserName!=null && tokenUserName.equals(userDetails.getUsername())&&!isExpired(token));
    }

    public boolean isExpired(String token){
        return extractExpiration(token).before(new Date());
    }


}
