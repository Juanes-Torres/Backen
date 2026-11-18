package com.kairos.Kairos_backend.infrastructure.security;

import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.infrastructure.logging.ContextoLog;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Se ejecuta en CADA petición: si trae "Authorization: Bearer <token>" válido,
 * le dice a Spring Security quién es el usuario y qué ROL tiene.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(PREFIJO.length());
            try {
                Claims claims = jwtService.leerClaims(token);
                String rol = claims.get("rol", String.class);
                Number idEmpresa = claims.get("idEmpresa", Number.class);   // solo empleados
                var usuario = new UsuarioAutenticado(
                        claims.get("id", Number.class).longValue(),
                        claims.getSubject(),
                        Rol.valueOf(rol),
                        idEmpresa == null ? null : idEmpresa.longValue());

                // "ROLE_ADMINISTRADOR", "ROLE_VENDEDOR"... es lo que usa hasRole(...)
                var autenticacion = new UsernamePasswordAuthenticationToken(
                        usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);

                // Desde aquí, cada línea de log de esta petición dice quién la hizo (sin email: es dato personal)
                MDC.put(ContextoLog.ID_USUARIO, String.valueOf(usuario.id()));
                if (usuario.idEmpresa() != null) {
                    MDC.put(ContextoLog.ID_EMPRESA, String.valueOf(usuario.idEmpresa()));
                }

            } catch (JwtException | IllegalArgumentException e) {
                // Token alterado o vencido: se sigue como anónimo y Spring responderá 401
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
