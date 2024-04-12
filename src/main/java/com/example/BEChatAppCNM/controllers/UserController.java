package com.example.BEChatAppCNM.controllers;

import com.example.BEChatAppCNM.entities.Role;
import com.example.BEChatAppCNM.entities.dto.FriendRequest;
import com.example.BEChatAppCNM.entities.dto.LoginRegisterResponse;
import com.example.BEChatAppCNM.entities.User;
import com.example.BEChatAppCNM.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final SimpMessagingTemplate messagingTemplate;


    @CrossOrigin(origins = {"http://10.0.2.2:8080"})
    @PostMapping("/register")
    public ResponseEntity register(@RequestBody User user) throws ExecutionException, InterruptedException {
        try {
            LoginRegisterResponse result = userService.saveUser(user);
            return  new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("Đăng ký lỗi", HttpStatus.BAD_REQUEST);
    }

    @CrossOrigin(origins = {"http://10.0.2.2:8080", "http://localhost:5173"})
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody User user) throws ExecutionException, InterruptedException {
        try {
            LoginRegisterResponse response = userService.checkAccountLogin(user);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity("Lỗi không đăng nhập được", HttpStatus.UNAUTHORIZED);
    }   

    @CrossOrigin(origins = {"http://10.0.2.2:8080", "http://localhost:5173"})
    @GetMapping("user/{phone}")
    public ResponseEntity checkExistPhone(@PathVariable String phone) throws ExecutionException, InterruptedException {
        boolean result = userService.checkExistPhoneNumber(phone);
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @CrossOrigin(origins = {"http://10.0.2.2:8080", "http://localhost:5173"})
    @GetMapping("user/details/{phone}")
    public ResponseEntity getUserDetails(@PathVariable String phone) throws ExecutionException, InterruptedException {
        try {
            Optional<User> result = userService.getUserDetailsByPhone(phone);
            return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
        } return new ResponseEntity<>("Lỗi lấy thông tin User", HttpStatus.BAD_REQUEST);
    }

    @MessageMapping("/user.testConnect")
    @SendTo("/topic/test")
    public User testConnect(String message) {
        User user = User.builder().phone("090988967").role(Role.USER).build();
        return user;
    }

    @MessageMapping("/user.userOnline")
    @SendTo("/topic/public")
    public User userOnline(User user) throws ExecutionException, InterruptedException {
        user.set_activated(true);
        userService.updateStatusUser(user.is_activated(), user.getPhone());
        return user;
    }

    @MessageMapping("/user.disconnectUser")
    @SendTo("/topic/public")
    public User disconnectUser(User user) throws ExecutionException, InterruptedException {
        user.set_activated(false);
        userService.updateStatusUser(user.is_activated(), user.getPhone());
        return user;
    }
}
