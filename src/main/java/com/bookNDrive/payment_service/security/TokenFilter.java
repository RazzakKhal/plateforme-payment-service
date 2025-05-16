package com.bookNDrive.payment_service.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TokenFilter extends OncePerRequestFilter {

    @Autowired
    JwtUtil jwtUtil;



    /**
     * Intercepte chaque requête HTTP pour extraire et valider le jeton JWT.
     *
     * @param request     HttpServletRequest représentant la requête HTTP.
     * @param response    HttpServletResponse représentant la réponse HTTP.
     * @param filterChain FilterChain pour continuer la chaîne de filtres.
     * @throws ServletException Si une erreur de servlet se produit.
     * @throws IOException      Si une erreur d'entrée/sortie se produit.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String jwt = header.substring(7);

            try {
                if(!jwtUtil.isTokenExpired(jwt)){
                    String username = jwtUtil.extractUsername(jwt);
                    Claims claims = jwtUtil.extractAllClaims(jwt); // méthode à implémenter


                    var auth = new UsernamePasswordAuthenticationToken(username, null,(Collection<? extends GrantedAuthority>) claims.get("role"));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            } catch (Exception e) {
                // Optionnel : logger ou ignorer
                System.out.println("⚠️ Token invalide : " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

}

