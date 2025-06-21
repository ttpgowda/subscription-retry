package com.thewealthweb.srbackend.user.repository;


import com.thewealthweb.srbackend.user.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
}
