package com.example.BEChatAppCNM.controllers;

import com.example.BEChatAppCNM.entities.dto.FriendRequest;
import com.example.BEChatAppCNM.entities.dto.MessageRequest;
import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.entities.User;
import com.example.BEChatAppCNM.services.ChatService;
import com.example.BEChatAppCNM.services.ConversationService;
import com.example.BEChatAppCNM.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
        String result = userService.saveUser(user);
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @CrossOrigin(origins = {"http://10.0.2.2:8080", "http://localhost:5173"})
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody User user) throws ExecutionException, InterruptedException {
        try {
            String result = userService.checkAccountSignIn(user);
            return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
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
        Optional<User> result = userService.getUserDetailsByPhone(phone);
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @MessageMapping("/user.userOnline")
    @SendTo("/user/public")
    public User userOnline(@Payload User user) throws ExecutionException, InterruptedException {
        System.out.println(user.toString());
        userService.updateStatusUser(true, user.getPhone());
        return user;
    }

    @MessageMapping("/user.disconnectUser")
    @SendTo("/user/public")
    public User disconnectUser(@Payload User user) throws ExecutionException, InterruptedException {
        userService.updateStatusUser(false, user.getPhone());
        return user;
    }

    @MessageMapping("/friend")
    public void sendFriendRequestNotification(@Payload FriendRequest friendRequest) {
        messagingTemplate.convertAndSendToUser(friendRequest.getReceiver_phone(), "/queue/friends", friendRequest);
    }

    @MessageMapping("/user.replyFriendRequest")
    @SendTo("/user/friendNoti")
    public  FriendRequest replyFriendRequest(@Payload FriendRequest friendRequest) throws ExecutionException, InterruptedException {
        if(friendRequest.isAceppted()) {
            userService.addFriend(friendRequest);
            return friendRequest;
        } else return friendRequest;
    }

}
