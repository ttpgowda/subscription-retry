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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List; // Import List

@Component
// Place your TenantFilter after Spring Security's main authentication filter
// You might need to experiment with the exact order depending on your SecurityConfig.
// A higher order number means it runs later.
// Often, after SecurityContextHolderFilter (usually order -100 or default)
// but before other business logic filters.
// If your SecurityConfig explicitly adds filters, adjust based on that.
@Order(2) // Or a higher number if you have other filters that need to run before
public class TenantFilter implements Filter {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String DEFAULT_TENANT = "dev-tenant";
    private static final String MAIN_DOMAIN = "thewealthweb.in"; // Your main domain

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Autowired
    private EntityManagerFactory entityManagerFactory; // Correct injection

    // Cache the SecretKey for performance
    private SecretKey cachedSecretKey;

    private SecretKey getSecretKey() {
        if (cachedSecretKey == null) {
            cachedSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        }
        return cachedSecretKey;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenantId = null; // Initialize to null

        EntityManager entityManager = null;
        Session session = null;
        boolean filterEnabled = false; // Flag to track if the filter was enabled

        try {
            // First, try to resolve tenant from JWT in case it's an authenticated request
            // This is done before the SecurityContextHolder might be populated by Spring Security's filters.
            tenantId = extractTenantFromJWT(httpRequest);

            // Fallback to other resolution methods if JWT tenant is not found
            if (tenantId == null) {
                tenantId = resolveTenantFromHeaderAndSubdomain(httpRequest);
            }

            // Fallback to default if no tenant is resolved
            if (tenantId == null || tenantId.isBlank()) {
                tenantId = DEFAULT_TENANT;
            }

            // Set tenant ID in context early for any components that need it
            TenantContext.setTenantId(tenantId);

            // Get an EntityManager and unwrap the Session
            entityManager = entityManagerFactory.createEntityManager();
            session = entityManager.unwrap(Session.class);

            if (session != null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                // Check if the current user is SUPER_ADMIN
                boolean isSuperAdmin = false;
                if (authentication != null && authentication.isAuthenticated()) {
                    isSuperAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
                }

                // IMPORTANT: Activate the Hibernate filter conditionally.
                // If SUPER_ADMIN, DO NOT activate the tenant filter
                if (!isSuperAdmin) {
                    session.enableFilter("tenantFilter").setParameter("tenantIdentifier", tenantId);
                    filterEnabled = true; // Mark as enabled
                } else {
                    // Log or handle the SUPER_ADMIN case where filter is bypassed
                    System.out.println("SUPER_ADMIN detected: Bypassing Hibernate tenant filter.");
                    // You might set a specific context property if other parts of your app
                    // need to know it's a SUPER_ADMIN global access.
                }
            }

            chain.doFilter(request, response);

        } finally {
            // Clear the tenant context regardless of success or failure
            TenantContext.clear();

            // Disable the Hibernate filter ONLY IF it was enabled
            if (session != null && filterEnabled) {
                session.disableFilter("tenantFilter");
            }
            // IMPORTANT: Close the EntityManager you created
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    // This method now only handles header and subdomain, JWT is handled separately first.
    private String resolveTenantFromHeaderAndSubdomain(HttpServletRequest request) {
        // 1. Header (for specific client requests or testing)
        String headerTenant = request.getHeader(TENANT_HEADER);
        if (headerTenant != null && !headerTenant.isBlank()) return headerTenant.trim();

        // 2. Subdomain (for branding/routing)
        String subdomainTenant = extractTenantFromSubdomain(request.getServerName());
        if (subdomainTenant != null) return subdomainTenant;

        return null; // Return null if not found
    }

    private String extractTenantFromJWT(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(getSecretKey()) // Use cached key
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // It's possible for a JWT to exist but not contain tenantId if it's
                // a system-level token or an old token.
                if (claims.containsKey("tenantId")) { // Check for existence before getting
                    return claims.get("tenantId", String.class);
                }
            } catch (Exception e) {
                // Log the exception for debugging. Do not throw, as other resolution methods might exist.
                System.err.println("JWT parsing failed during tenant extraction: " + e.getMessage());
            }
        }
        return null; // Return null if JWT not present, invalid, or no tenantId claim
    }

    private String extractTenantFromSubdomain(String host) {
        if (host != null && host.endsWith(MAIN_DOMAIN)) {
            String[] parts = host.split("\\.");
            // Example: "dev.thewealthweb.in" -> parts[0]="dev"
            // Example: "app.dev.thewealthweb.in" -> parts[0]="app" (might not be desired)
            // You might need more sophisticated subdomain parsing if your tenant IDs aren't always the first part.
            // For "dev.thewealthweb.in", parts.length would be 3 (dev, thewealthweb, in)
            if (parts.length > 2 && !parts[0].equals("www")) { // Avoid "www" if it's a common prefix
                return parts[0];
            }
        }
        return null;
    }
}