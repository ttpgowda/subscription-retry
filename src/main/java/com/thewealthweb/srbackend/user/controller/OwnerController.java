package com.thewealthweb.srbackend.user.controller;

import com.thewealthweb.srbackend.user.entity.Owner;
import com.thewealthweb.srbackend.user.service.OwnerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/owners")
public class OwnerController {

    private final OwnerService service;

    public OwnerController(OwnerService service) {
        this.service = service;
        System.out.println("OwnerController initialized with service: " + service);
    }

    @PostMapping
    public Owner createOwner(@RequestParam String name) {
        return service.save(name);
    }

    @GetMapping
    public List<Owner> getOwners() {
        System.out.println("OwnerController instance: " + this);
        System.out.println("Service instance used in controller: " + service);
        List<Owner> owners = service.getAll();
        System.out.println("Fetched owners = " + owners);

        if (owners == null || owners.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No owners found");
        }
        return owners;
    }
}
