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
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @CrossOrigin(origins = {"http://10.0.2.2:8080"})
    @PostMapping("/register")
    public ResponseEntity register(@RequestBody User user) throws ExecutionException, InterruptedException {
        String result = userService.saveUser(user);
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @CrossOrigin(origins = {"http://10.0.2.2:8080"})
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody User user) throws ExecutionException, InterruptedException {
        String result = userService.checkAccountSignIn(user);
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @CrossOrigin(origins = {"http://10.0.2.2:8080"})
    @GetMapping("user/{phone}")
    public ResponseEntity checkExistPhone(@PathVariable String phone) throws ExecutionException, InterruptedException {
        boolean result = userService.checkExistPhoneNumber(phone);
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @CrossOrigin(origins = {"http://10.0.2.2:8080"})
    @GetMapping("user/details/{phone}")
    public ResponseEntity getUserDetails(@PathVariable String phone) throws ExecutionException, InterruptedException {
        Optional<User> result = userService.getUserDetailsByPhone(phone);
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }
}
