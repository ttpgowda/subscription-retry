package com.thewealthweb.crmbackend.tenant.config;


import org.hibernate.cfg.MultiTenancySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class HibernateMultiTenancyConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            JpaVendorAdapter jpaVendorAdapter,
            TenantIdentifierResolver tenantResolver
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.multiTenancy", "DISCRIMINATOR"); // Correct string form
        properties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);
        properties.put("hibernate.hbm2ddl.auto", "update"); // ✅ add this here
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.thewealthweb.crmbackend");
        em.setJpaVendorAdapter(jpaVendorAdapter);
        em.setJpaPropertyMap(properties);
        return em;
    }
}