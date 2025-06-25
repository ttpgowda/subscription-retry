package com.thewealthweb.srbackend.tenant.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManagerFactory; // <--- Make sure this is imported
import jakarta.persistence.EntityManager; // <--- Make sure this is imported
import org.springframework.beans.factory.annotation.Autowired; // <--- Make sure this is imported

import javax.crypto.SecretKey;
import java.io.IOException;


@Component
@Order(1)
public class TenantFilter implements Filter {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String DEFAULT_TENANT = "dev-tenant";
    private static final String MAIN_DOMAIN = "thewealthweb.in";

    @Value("${jwt.secret}")
    private String jwtSecret;

    // --- CRITICAL CHANGE HERE ---
    @Autowired // <--- Use @Autowired for EntityManagerFactory
    private EntityManagerFactory entityManagerFactory; // <--- Inject EntityManagerFactory


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenantId = resolveTenant(httpRequest);

        TenantContext.setTenantId(tenantId);

        // --- HIBERNATE FILTER ACTIVATION ---
        EntityManager entityManager = null; // Declare it here
        Session session = null;
        try {
            // Get an EntityManager instance from the factory
            entityManager = entityManagerFactory.createEntityManager(); // <--- Create EntityManager
            session = entityManager.unwrap(Session.class);

            if (session != null) {
                session.enableFilter("tenantFilter").setParameter("tenantIdentifier", tenantId);
            }

            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            if (session != null) {
                session.disableFilter("tenantFilter");
            }
            // IMPORTANT: Close the EntityManager you created
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    private String resolveTenant(HttpServletRequest request) {
        // 1. JWT (most authoritative after authentication)
        String jwtTenant = extractTenantFromJWT(request);
        if (jwtTenant != null) return jwtTenant;

        // 2. Header (for specific client requests or testing)
        String headerTenant = request.getHeader(TENANT_HEADER);
        if (headerTenant != null && !headerTenant.isBlank()) return headerTenant.trim();

        // 3. Subdomain (for branding/routing)
        String subdomainTenant = extractTenantFromSubdomain(request.getServerName());
        if (subdomainTenant != null) return subdomainTenant;

        // 4. Default tenant (for unauthenticated or system-wide requests)
        return DEFAULT_TENANT;
    }

    private String extractTenantFromJWT(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                return claims.get("tenant_id", String.class);
            } catch (Exception e) {
                System.err.println("JWT parsing failed during tenant extraction: " + e.getMessage());
            }
        }
        return null;
    }

    private String extractTenantFromSubdomain(String host) {
        if (host != null && host.endsWith(MAIN_DOMAIN)) {
            String[] parts = host.split("\\.");
            if (parts.length > 2) {
                return parts[0];
            }
        }
        return null;
    }
}