package com.project.retailproject.config;

import com.project.retailproject.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .cors(cors -> cors.configure(http))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Public — no token needed
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // /api/users/me — ALL authenticated roles can access
                        // MUST be before /api/users/** so it is not blocked by ADMIN-only rule
                        .requestMatchers("/api/users/me").authenticated()

                        // User management — ADMIN only
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // Sales — ADMIN + STORE_ASSOCIATE + STORE_MANAGER
                        .requestMatchers("/api/sales/**").hasAnyRole("ADMIN", "STORE_ASSOCIATE", "STORE_MANAGER")

                        // Inventory — ADMIN + INVENTORY_MANAGER
                        .requestMatchers("/api/inventory/**").hasAnyRole("ADMIN", "INVENTORY_MANAGER")
                        .requestMatchers("/api/purchase-orders/**").hasAnyRole("ADMIN", "INVENTORY_MANAGER")

                        // Finance — ADMIN + FINANCE_OFFICER
                        .requestMatchers("/api/invoices/**").hasAnyRole("ADMIN", "FINANCE_OFFICER")
                        .requestMatchers("/api/payments/**").hasAnyRole("ADMIN", "FINANCE_OFFICER")

                        // Compliance — ADMIN + COMPLIANCE_OFFICER
                        .requestMatchers("/api/compliance-reports/**").hasAnyRole("ADMIN", "COMPLIANCE_OFFICER")

                        // KPI — ADMIN + STORE_MANAGER
                        .requestMatchers("/api/kpi-reports/**").hasAnyRole("ADMIN", "STORE_MANAGER")

                        // Products and Catalogs — all authenticated
                        .requestMatchers("/api/products/**").authenticated()
                        .requestMatchers("/api/catalogs/**").authenticated()

                        // Notifications — all authenticated
                        .requestMatchers("/api/notifications/**").authenticated()

                        // Audit logs — GET only for ADMIN + COMPLIANCE_OFFICER
                        .requestMatchers(HttpMethod.GET, "/api/audit-logs/**")
                        .hasAnyRole("ADMIN", "COMPLIANCE_OFFICER")
                        .requestMatchers(HttpMethod.POST, "/api/audit-logs/**")
                        .denyAll()

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