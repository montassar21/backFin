package com.FST.GestionDesVentes.config;

import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.FST.GestionDesVentes.filter.JwtFilter;
import com.FST.GestionDesVentes.service.UserRegistrationService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserRegistrationService registrationService;

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/authenticate",
                                "/", // Home
                                "/contact", // Contact
                                "/about", // About
                                "/blog", // Blog
                                "/shop", // Shop
                                "/shop-details/**", // Shop Details
                                "/shopping-cart", // Shopping Cart
                                "/check-out", // Check Out
                                "/blog-detail", // Blog Detail
                                "/facture/**", // Facture Details
                                "/loginuser", // Login User
                                "/registeruser", // Register User
                                "/gettotalusers", // Get Total Users
                                "/doctorlist", // Doctor List
                                "/acceptstatus/{email}", // Accept Status
                                "/rejectstatus/{email}", // Reject Status
                                "/panier/{email}",
                                "/userlist", // User List
                                "/profileDetails/{email}", // Profile Details
                                "/updateuser", // Update User
                                "/bookNewAppointment", // Book New Appointment
                                "/admin/login",
                                "/categories/**","/clients/**","/facture/**","/commandes/**","/fournisseur/**","/produits/**",
                                "/produits/list", "/produits/add", "/categories/list", "/categories/add",
                                "/clients/list", "/clients/add", "/commandes/list", "/commandes/add", "/facture/list",
                                "/facture/add", "/fournisseur/list", "/fournisseur/add",
                                "/panier/addProduitToPanier/{idUser}/{idProduit}",
                                "/panier/removeProduitFromPanier/{idPanier}/{idProduit}",
                                "/panier/increaseQuantity/{idPanier}/{idProduit}",
                                "/panier/decreaseQuantity/{idPanier}/{idProduit}")
                        .permitAll()
                        .anyRequest().fullyAuthenticated())
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            AccessDeniedHandler defaultAccessDeniedHandler = new AccessDeniedHandlerImpl();
                            defaultAccessDeniedHandler.handle(request, response, accessDeniedException);
                        }))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http
                .getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(registrationService);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
