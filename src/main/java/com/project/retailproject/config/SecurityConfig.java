package com.project.retailproject.config;

import com.project.retailproject.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ✅ Public endpoints — no token needed
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 🔒 Admin only
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // 🔒 Inventory Manager + Admin
                        .requestMatchers("/api/inventory/**").hasAnyRole("ADMIN", "INVENTORY_MANAGER")
                        .requestMatchers("/api/purchase-orders/**").hasAnyRole("ADMIN", "INVENTORY_MANAGER")

                        // 🔒 Finance Officer + Admin
                        .requestMatchers("/api/invoices/**").hasAnyRole("ADMIN", "FINANCE_OFFICER")
                        .requestMatchers("/api/payments/**").hasAnyRole("ADMIN", "FINANCE_OFFICER")

                        // 🔒 Compliance Officer + Admin
                        .requestMatchers("/api/compliance-reports/**").hasAnyRole("ADMIN", "COMPLIANCE_OFFICER")
                        .requestMatchers("/api/audit-logs/**").hasAnyRole("ADMIN", "COMPLIANCE_OFFICER")

                        // 🔒 Store Manager + Admin
                        .requestMatchers("/api/kpi-reports/**").hasAnyRole("ADMIN", "STORE_MANAGER")

                        // 🔒 All authenticated users
                        .requestMatchers("/api/products/**").authenticated()
                        .requestMatchers("/api/catalogs/**").authenticated()
                        .requestMatchers("/api/sales/**").authenticated()
                        .requestMatchers("/api/notifications/**").authenticated()

                        // Everything else needs auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}