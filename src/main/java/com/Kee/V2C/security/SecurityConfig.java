package com.Kee.V2C.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,UserDetailsService userDetailsService){
        this.userDetailsService=userDetailsService;
        this.jwtAuthenticationFilter=jwtAuthenticationFilter;
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());     // Uses your BCrypt bean below
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {


               // Disable CSRF (Cross-Site Request Forgery)
               // We disable this because we are building a stateless REST API,
               // and CSRF is primarily for stateful session-based browser apps.
               http.csrf(customizer->customizer.disable())

                //providing http basic security
                 //http.httpBasic(Customizer.withDefaults());
               .authorizeHttpRequests(configurer ->
                configurer
                        .requestMatchers("/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html","/index.html","/shop.html","/orders.html", "/account.html"
                                , "/admin.html", "/api.js","/app.css","/uploads/**","/vendor.html"
                                ,"/api/categories/**","/api/brands/**","/api/notifications/stream",
                                        "/").permitAll()// Make registration public
                        .requestMatchers("/api/products/vendor/**").hasRole("SELLER")
                        .requestMatchers("/api/product-models/**").hasAnyRole("SELLER","ADMIN")
                        .requestMatchers("/api/vendors/**","/api/shops/**","/api/stocks/**","/api/sub-orders/**").hasAnyRole("SELLER","ADMIN")
                        .requestMatchers("/api/products/search/customer").hasRole("CUSTOMER")
                        .requestMatchers("/api/products/search/admin").hasRole("ADMIN")
                        .requestMatchers("/api/products","/api/products/*").hasAnyRole("CUSTOMER","ADMIN","SELLER")
                        .requestMatchers("/api/test/my-profile","/api/carts/**","/api/checkouts"
                                ,"/api/invoices/**","/api/customers/**","/api/orders/**").hasAnyRole("CUSTOMER","ADMIN")

                        .anyRequest().authenticated() //any request of those are protected
        ).sessionManagement(session->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                //we are using UsernamePasswordAuthenticationFilter as a positional reference to
        //position when the jwtAuthenticationFilter is placed, it will be short circuited because
        //the user will be already authenticated when its time for UsernamePasswordAuthenticationFilter
        return http.build();
    }
}
