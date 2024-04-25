package com.example.BEChatAppCNM.controllers;

import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.entities.User;
import com.example.BEChatAppCNM.entities.dto.FriendRequest;
import com.example.BEChatAppCNM.services.FriendService;
import com.example.BEChatAppCNM.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final FriendService friendService;

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
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/user/friend-request/{currentPhone}")
    public ResponseEntity getFriendRequestList(@PathVariable String currentPhone) throws ExecutionException, InterruptedException {
        List<FriendRequest> friendRequests = friendService.getListFriendRequest(currentPhone);
        if(!friendRequests.isEmpty()) {
            return new ResponseEntity(friendRequests, HttpStatus.OK);
        } else return new ResponseEntity("Danh sách lời mời kết bạn rỗng", HttpStatus.NOT_FOUND);
    }

    @MessageMapping("/user.addFriend")
    public ResponseEntity sendFriendRequest(FriendRequest friendRequest) {
        FriendRequest friendRequestReturn = friendService.addInviteFriendRequest(friendRequest);
        messagingTemplate.convertAndSendToUser(friendRequest.getReceiver_phone(), "/queue/friend-request", friendRequest);
        messagingTemplate.convertAndSendToUser(friendRequest.getSender_phone(), "/queue/friend-request", friendRequest);
        return new ResponseEntity<>(friendRequestReturn, HttpStatus.OK);
    }

    @MessageMapping("/user.replyFriendRequest")
    public  FriendRequest replyFriendRequest(FriendRequest friendRequest) throws ExecutionException, InterruptedException {
        if(friendRequest.isAceppted()) {
             Conversation conversation = userService.addFriend(friendRequest);

             conversation.getMembers().forEach((memberPhone) -> {
                messagingTemplate.convertAndSendToUser(memberPhone, "/queue/chat", conversation);
            });

             messagingTemplate.convertAndSendToUser(friendRequest.getSender_phone(), "/queue/friend-reply", friendRequest);
             messagingTemplate.convertAndSendToUser(friendRequest.getReceiver_phone(), "/queue/friend-reply", friendRequest);

             friendService.deleteFriendRequest(friendRequest.getId());
             return friendRequest;
        } else return friendRequest;
    }
}
