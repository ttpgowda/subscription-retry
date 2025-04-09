package com.thewealthweb.crmbackend.user.repository;


import com.thewealthweb.crmbackend.user.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
}
