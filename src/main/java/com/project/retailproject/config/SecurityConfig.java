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
                .cors(cors -> cors.configure(http)) // ADD THIS LINE — tells Spring Security to use CorsConfig
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        .requestMatchers("/api/compliance-reports/**").permitAll()
                        .requestMatchers("/api/kpi-reports/**").permitAll()


                        .requestMatchers("/api/sales/**").hasAnyRole("ADMIN", "STORE_ASSOCIATE", "STORE_MANAGER")

                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        .requestMatchers("/api/inventory/**").hasAnyRole("ADMIN", "INVENTORY_MANAGER")
                        .requestMatchers("/api/purchase-orders/**").hasAnyRole("ADMIN", "INVENTORY_MANAGER")

                        .requestMatchers("/api/invoices/**").hasAnyRole("ADMIN", "FINANCE_OFFICER")
                        .requestMatchers("/api/payments/**").hasAnyRole("ADMIN", "FINANCE_OFFICER")

                        .requestMatchers("/api/compliance-reports/**").hasAnyRole("ADMIN", "COMPLIANCE_OFFICER")
                        .requestMatchers("/api/audit-logs/**").hasAnyRole("ADMIN", "COMPLIANCE_OFFICER")

                        .requestMatchers("/api/kpi-reports/**").hasAnyRole("ADMIN", "STORE_MANAGER")

                        .requestMatchers("/api/products/**").authenticated()
                        .requestMatchers("/api/catalogs/**").authenticated()
                        .requestMatchers("/api/notifications/**").authenticated()

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