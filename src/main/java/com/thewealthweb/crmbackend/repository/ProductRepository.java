package com.thewealthweb.crmbackend.repository;

import com.thewealthweb.crmbackend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}