package com.example.BEChatAppCNM.controllers;

import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.entities.dto.MessageRequest;
import com.example.BEChatAppCNM.services.ChatService;
import com.example.BEChatAppCNM.services.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ConversationService conversationService;

    private final ChatService chatService;

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/user.creatConversation")
    public Conversation createConversation(@Payload Conversation conversation) {

        Conversation conversationResult = conversationService.addConversation(conversation);

        conversationResult.getMembers().forEach((memberPhone) -> {
            messagingTemplate.convertAndSendToUser(memberPhone, "/queue/chat", conversationResult);
        });

        return conversationResult;
    }

    @CrossOrigin("http://localhost:5173")
    @GetMapping("user/messages/{creator_phone}")
    public ResponseEntity findListConversation(@PathVariable String creator_phone) throws ExecutionException, InterruptedException {
        List<Conversation> conversationList = conversationService.findListConversationByCreatorPhone(creator_phone);
        if(conversationList.size() != 0) {
            return new ResponseEntity<>(conversationList, HttpStatus.OK);
        } else return new ResponseEntity<>("Không tìm thấy hội thoại nào", HttpStatus.NOT_FOUND);
    }

    @MessageMapping("/chat")
    public MessageRequest saveMessage(@Payload MessageRequest messageRequest) {
        chatService.saveMessage(messageRequest);
        messageRequest.getMembers().forEach(members -> {
            messagingTemplate.convertAndSendToUser(members, "/queue/messages", messageRequest);
        });
        return messageRequest;
    }
}
