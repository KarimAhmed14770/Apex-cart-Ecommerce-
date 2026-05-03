package com.Kee.V2C.security;

import com.Kee.V2C.Repository.VendorRepository;
import com.Kee.V2C.entity.Credential;
import com.Kee.V2C.entity.Role;
import com.Kee.V2C.entity.Vendor;
import com.Kee.V2C.enums.UserRoles;
import com.Kee.V2C.exception.ResourceNotFoundException;
import com.Kee.V2C.service.Authentication.JwtService;
import com.Kee.V2C.service.notification.NotificationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final NotificationService notificationService;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService,UserDetailsService userDetailsService,
                                   NotificationService notificationService){
        this.jwtService=jwtService;
        this.userDetailsService=userDetailsService;
        this.notificationService=notificationService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response
            ,@NonNull FilterChain filterChain) throws ServletException, IOException{
        final String authorizationHeader=request.getHeader("Authorization");
        final String requestUrl=request.getRequestURI();
        final String ticket=request.getParameter("token");
        final String jwt;
        final String userName;

        if(requestUrl.contains("/api/notifications/stream") && ticket!=null){
            authenticateViaTicket(ticket,request);
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null && authorizationHeader != null
        && authorizationHeader.startsWith("Bearer ")) {
            jwt=authorizationHeader.substring(7);//trimming the bearer word
            userName=jwtService.extractUserName(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

            if (jwtService.isTokenValid(jwt, userDetails) &&userDetails.isEnabled()) {
                // Create the authentication object for spring security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Credentials are not needed after JWT validation
                        userDetails.getAuthorities()
                );

                // Add request-specific details (like IP address) to the token
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // save it to SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        /*If userName is null, it means the JWT was corrupted or didn't contain a "Subject" claim.
           The Filter's Action: It skips the if block, meaning it does not set anything in the
           SecurityContextHolder.then The request moves to the next filter with an empty security context.
           When the request hits the final "Authorization Gate,"
           Spring will see there is no authenticated user. If the page is protected (like /api/admin),
           the user gets a 403 Forbidden.
        */
        filterChain.doFilter(request, response);
    }

    private void authenticateViaTicket(String ticket, HttpServletRequest request) {
        Long vendorId = notificationService.validateAndRemoveTicket(ticket);
        if (vendorId != null) {


            Credential credential = new Credential(vendorId, new Role(UserRoles.ROLE_SELLER));
            UserDetails userDetails = new UserDetailsImpl(credential);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null, // Credentials are not needed after JWT validation
                    userDetails.getAuthorities()
            );
            // Add request-specific details (like IP address) to the token
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // save it to SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }
}
