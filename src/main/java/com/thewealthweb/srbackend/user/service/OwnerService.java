package com.thewealthweb.srbackend.user.service;

import com.thewealthweb.srbackend.user.entity.Owner;
import com.thewealthweb.srbackend.user.repository.OwnerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OwnerService {

    private final OwnerRepository repository;

    public OwnerService(OwnerRepository repository) {
        this.repository = repository;
    }

    public Owner save(String name) {
        Owner owner =Owner
                .builder()
                .name(name)
                .build();

        return repository.save(owner);
    }

    public List<Owner> getAll() {
        List<Owner>  ownerList = repository.findAll();
        System.out.println("OwnerService instance (getAll): " + this);
        System.out.println("Owner List: " + ownerList);
        return ownerList;
    }
}