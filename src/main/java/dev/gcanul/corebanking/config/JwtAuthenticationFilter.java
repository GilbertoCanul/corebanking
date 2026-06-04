package dev.gcanul.corebanking.config;

import dev.gcanul.corebanking.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor // Crea automáticamente el constructor para las dependencias final
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extraer el header Authorization
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        String userEmail = null;

        // 2. Si no hay header o no empieza con "Bearer ", saltamos al siguiente filtro
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token
        jwt = authHeader.substring(7);

        // --- EL BLOQUE SENIOR: Blindaje contra tokens expirados, mal formados o nulos ---
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Si el token falló (ej. ExpiredJwtException o SignatureException),
            // simplemente pasamos la petición al siguiente filtro sin setear la autenticación.
            // Esto forzará a Spring Security a lanzar un 401 usando tu EntryPoint.
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Validar si el usuario existe y no está ya autenticado
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                // 5. Crear el token de autenticación de Spring Security
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. ¡IMPORTANTE! Guardar la autenticación en el contexto
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 7. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}