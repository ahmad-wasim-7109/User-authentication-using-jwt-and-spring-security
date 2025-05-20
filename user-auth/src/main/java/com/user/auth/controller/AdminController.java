package com.user.auth.controller;

import com.user.auth.entity.User;
import com.user.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final UserRepository userRepository;
     @GetMapping("/users")
     public ResponseEntity<List<User>> getAllUsers() {
         List<User> users = userRepository.findAll();
         log.info("Fetched {} users", users.size());
         return ResponseEntity.ok(users);
     }
}