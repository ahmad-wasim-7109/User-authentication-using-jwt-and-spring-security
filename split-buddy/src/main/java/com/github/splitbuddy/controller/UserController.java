package com.github.splitbuddy.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1/user")
@Tag(name = "3. User Controller")
public class UserController {

     @GetMapping("/profile")
     public ResponseEntity<String> getUserProfile() {
         // Logic to retrieve user profile
         System.out.println("Welcome to the User Profile API");
         return ResponseEntity.ok("Welcome to the User Profile API");
     }
}
