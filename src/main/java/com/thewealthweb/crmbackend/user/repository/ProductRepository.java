package com.thewealthweb.crmbackend.user.repository;

import com.thewealthweb.crmbackend.user.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}