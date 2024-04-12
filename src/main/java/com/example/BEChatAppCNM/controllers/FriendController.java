package com.example.BEChatAppCNM.controllers;

import com.example.BEChatAppCNM.entities.dto.FriendRequest;
import com.example.BEChatAppCNM.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
public class FriendController {

    private final UserService userService;

    private final SimpMessagingTemplate messagingTemplate;

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
