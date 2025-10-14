package account.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final int passwordEncoderStrength = 13;

    public SecurityConfig(RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(passwordEncoderStrength);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (req, res, ex) -> {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            String body = """
            {"timestamp":"%s","status":403,"error":"Forbidden","message":"Access Denied!","path":"%s"}
            """.formatted(LocalDateTime.now(), req.getRequestURI());
            res.getWriter().write(body);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(
                        ex -> ex
                                                                    .authenticationEntryPoint(restAuthenticationEntryPoint)
                                                                    .accessDeniedHandler(accessDeniedHandler())

                ) // Handle auth errors
                .csrf(csrf -> csrf.disable()) // For Postman
                .headers(headers -> headers.frameOptions().disable()) // For the H2 console
                .authorizeHttpRequests(auth -> auth  // manage access
                                .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/auth/changepass").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/empl/payment").hasAnyRole("USER", "ACCOUNTANT")
                                .requestMatchers(HttpMethod.POST, "/api/acct/payments").hasRole("ACCOUNTANT")
                                .requestMatchers(HttpMethod.PUT, "/api/acct/payments").hasRole("ACCOUNTANT")
                                .requestMatchers( "/api/admin/**").hasRole("ADMINISTRATOR")
                                .requestMatchers("/h2-console/**").permitAll() //
                                .requestMatchers("/actuator/shutdown").permitAll()
                                .anyRequest().authenticated()
                        // other matchers
                )

                .sessionManagement(sessions -> sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                );

        return http.build();
    }
}
