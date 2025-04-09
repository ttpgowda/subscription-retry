#### File Structure need to maintain 

```
com.yourcompany.yourapp
│
├── config/                 # General application-level configuration
│   ├── AppConfig.java
│   ├── JpaAuditConfig.java
│   └── SecurityConfig.java
│
├── common/                 # Shared utilities and base classes
│   ├── entity/
│   │   ├── BaseEntity.java          # For auditing
│   │   └── TenantAwareEntity.java   # If tenant needs to be part of base
│   ├── dto/
│   ├── constants/
│   ├── exceptions/
│   └── utils/
│
├── security/               # JWT, filters, auth providers
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   ├── CustomUserDetails.java
│   └── SecurityUtils.java
│
├── tenant/                 # Multi-tenancy setup
│   ├── TenantContext.java
│   ├── CurrentTenantIdentifierResolverImpl.java
│   ├── TenantEntity.java
│   └── TenantService.java
│
├── user/                   # Auth system - users, roles, login
│   ├── entity/
│   │   ├── User.java
│   │   └── Role.java
│   ├── dto/
│   ├── controller/
│   ├── repository/
│   ├── service/
│   └── security/
│       └── UserDetailsServiceImpl.java
│
├── module1/                # Business domain (e.g., CRM)
│   ├── entity/
│   ├── dto/
│   ├── controller/
│   ├── repository/
│   └── service/
│
├── module2/                # Another domain (e.g., Projects)
│   ├── entity/
│   ├── controller/
│   ├── service/
│   └── repository/
│
└── Application.java        # Main class
```

#### For getting the File Structure
```
/F /A > project-structure.txt
```