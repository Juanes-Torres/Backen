package com.kairos.Kairos_backend.infrastructure.config;

import com.kairos.Kairos_backend.infrastructure.security.JwtAuthenticationFilter;
import com.kairos.Kairos_backend.infrastructure.security.NoAutenticadoHandler;
import com.kairos.Kairos_backend.infrastructure.security.SinPermisoHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // activa @PreAuthorize en los controllers
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final NoAutenticadoHandler noAutenticado;
    private final SinPermisoHandler sinPermiso;
    private final String[] origenesPermitidos;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          NoAutenticadoHandler noAutenticado,
                          SinPermisoHandler sinPermiso,
                          @Value("${kairos.cors.allowed-origins}") String[] origenesPermitidos) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.noAutenticado = noAutenticado;
        this.sinPermiso = sinPermiso;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())                 // API REST con JWT: no usa cookies de sesión
                .cors(Customizer.withDefaults())              // usa el bean corsConfigurationSource()
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Preflight de CORS que hace el navegador
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Registro de clientes y login: públicos
                        .requestMatchers(HttpMethod.POST, "/api/usuarios", "/api/usuarios/login").permitAll()
                        // Catálogo web: cualquiera puede consultarlo (RF03)
                        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**",
                                "/api/categorias", "/api/categorias/**").permitAll()
                        // Documentación Swagger / OpenAPI: pública
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Todo lo demás exige token (los roles se revisan con @PreAuthorize)
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(noAutenticado)   // 401
                        .accessDeniedHandler(sinPermiso)           // 403
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** CORS: permite que el frontend (React en otro puerto) llame a esta API. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origenesPermitidos));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
