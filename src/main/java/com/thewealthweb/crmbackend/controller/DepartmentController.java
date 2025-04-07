package com.thewealthweb.crmbackend.controller;

import com.thewealthweb.crmbackend.exception.DepartmentNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DepartmentController {

    @GetMapping("/department")
    public String getDepartment() {
        // Simulate an exception
        throw new DepartmentNotFoundException("Department not found!");
    }
}