package com.thewealthweb.crmbackend.tenant.config;

import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.io.IOException;
import java.security.Key;

@Component
public class TenantFilter implements Filter {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String DEFAULT_TENANT = "dev-tenant";
    private static final String MAIN_DOMAIN = "thewealthweb.in";
    private static final String JWT_SECRET = "2af31640e0fd02cf3ce8148deeed774bd119b05fcecb91c2ea90386929a18692";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenantId = resolveTenant(httpRequest);

        TenantContext.setTenantId(tenantId);
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveTenant(HttpServletRequest request) {
        // 1. JWT
        String jwtTenant = extractTenantFromJWT(request);
        if (jwtTenant != null) return jwtTenant;

        // 2. Header
        String headerTenant = request.getHeader(TENANT_HEADER);
        if (headerTenant != null && !headerTenant.isBlank()) return headerTenant.trim();

        // 3. Subdomain
        String subdomainTenant = extractTenantFromSubdomain(request.getServerName());
        if (subdomainTenant != null) return subdomainTenant;

        // 4. Default
        return DEFAULT_TENANT;
    }

    private String extractTenantFromJWT(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7); // Skip "Bearer "
                Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());

                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                return claims.get("tenant_id", String.class);
            } catch (Exception e) {
                System.out.println("JWT parsing failed: " + e.getMessage());
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