package com.example.BEChatAppCNM.controllers;

import com.example.BEChatAppCNM.entities.User;
import com.example.BEChatAppCNM.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody User user) throws ExecutionException, InterruptedException {
        String result = userService.saveUser(user);
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @PostMapping("/signin")
    public ResponseEntity signIn(@RequestBody User user) throws ExecutionException, InterruptedException {
        boolean result = userService.checkAccountSignIn(user);
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{phone}")
    public ResponseEntity checkExistPhone(@PathVariable String phone) throws ExecutionException, InterruptedException {
        boolean result = userService.checkExistPhoneNumber(phone);
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }
    @GetMapping("/details/{phone}")
    public ResponseEntity getUserDetails(@PathVariable String phone) throws ExecutionException, InterruptedException {
        Optional<User> result = userService.getUserDetails(phone);
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }
}
