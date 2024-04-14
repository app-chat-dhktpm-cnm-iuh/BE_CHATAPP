package com.example.BEChatAppCNM.controllers;

import com.example.BEChatAppCNM.entities.User;
import com.example.BEChatAppCNM.entities.dto.FriendRequest;
import com.example.BEChatAppCNM.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
public class FriendController {

    private final UserService userService;

    private final SimpMessagingTemplate messagingTemplate;

    @CrossOrigin("http://localhost:5173")
    @GetMapping("/user/friends/{currentPhone}")
    public ResponseEntity getFriendListCurrentPhone(@PathVariable String currentPhone) throws ExecutionException, InterruptedException {
        try {
            List<User> userList = userService.getListFriendByPhone(currentPhone);
            if(!userList.isEmpty()) {
                return new ResponseEntity<>(userList, HttpStatus.OK);
            } else return new ResponseEntity<>("Không tìm thấy bạn bè nào", HttpStatus.NOT_FOUND);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return new ResponseEntity("Lỗi tìm kiếm bạn bè", HttpStatus.BAD_REQUEST);
        }
    }

    @MessageMapping("/user.addFriend")
    public void sendFriendRequestNotification(FriendRequest friendRequest) {
        messagingTemplate.convertAndSendToUser(friendRequest.getReceiver_phone(), "/queue/friends", friendRequest);
    }

    @MessageMapping("/user.replyFriendRequest")
    @SendTo("/topic/friendNoti")
    public  FriendRequest replyFriendRequest(FriendRequest friendRequest) throws ExecutionException, InterruptedException {
        if(friendRequest.isAceppted()) {
            userService.addFriend(friendRequest);
            return friendRequest;
        } else return friendRequest;
    }
}
