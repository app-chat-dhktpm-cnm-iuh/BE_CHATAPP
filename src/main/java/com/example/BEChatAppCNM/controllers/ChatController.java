package com.example.BEChatAppCNM.controllers;

import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.entities.User;
import com.example.BEChatAppCNM.entities.dto.ConversationResponse;
import com.example.BEChatAppCNM.entities.dto.MessageRequest;
import com.example.BEChatAppCNM.services.ChatService;
import com.example.BEChatAppCNM.services.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ConversationService conversationService;

    private final ChatService chatService;

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/user.creatConversation")
    public ConversationResponse createConversation(Conversation conversation) {
        ConversationResponse conversationResult = conversationService.addConversation(conversation);

        conversation.getMembers().forEach((memberPhone) -> {
            messagingTemplate.convertAndSendToUser(memberPhone, "/queue/chat", conversationResult);
        });

        return conversationResult;
    }

    @CrossOrigin("http://localhost:5173")
    @GetMapping("user/messages/{creator_phone}")
    public ResponseEntity findListConversation(@PathVariable String creator_phone) throws ExecutionException, InterruptedException {
        List<ConversationResponse> conversationList = conversationService.findListConversationByCreatorPhone(creator_phone);
        if(conversationList.size() != 0) {
            return new ResponseEntity<>(conversationList, HttpStatus.OK);
        } else return new ResponseEntity<>("Không tìm thấy hội thoại nào", HttpStatus.NOT_FOUND);
    }

//    @CrossOrigin("http://localhost:5173")
//    @GetMapping("user/message/{senderPhone}/{receiverPhone}")
//    public ResponseEntity findConversationBySenderPhoneAndReceiverPhone(@PathVariable String senderPhone, @PathVariable String receiverPhone) throws ExecutionException, InterruptedException {
//        ConversationResponse conversationResponse = conversationService.getConversationBySenderPhoneAndReceiverPhone(senderPhone, receiverPhone);
//
//        if(conversationResponse != null) {
//            return new ResponseEntity<>(conversationResponse, HttpStatus.OK);
//        } return new ResponseEntity<>("Không tìm thấy", HttpStatus.NOT_FOUND);
//    }

    @CrossOrigin("http://localhost:5173")
    @GetMapping("user/conversations/{conversation_id}")
    public ResponseEntity findConversationByID(@PathVariable String conversation_id) throws ExecutionException, InterruptedException {
        ConversationResponse conversation = conversationService.getConversationById(conversation_id);
        if(conversation != null) {
            return new ResponseEntity<>(conversation, HttpStatus.OK);
        } else return new ResponseEntity<>("Không tìm thấy hội thoại nào", HttpStatus.NOT_FOUND);
    }

    @MessageMapping("/chat")
    public MessageRequest saveMessage(MessageRequest messageRequest) throws ExecutionException, InterruptedException {
        chatService.saveMessage(messageRequest);
        messageRequest.getMembers().forEach(member_phone -> {
            messagingTemplate.convertAndSendToUser(member_phone, "/queue/messages", messageRequest);
            try {
                ConversationResponse conversationResponse = conversationService.getConversationById(messageRequest.getConversation_id());
                messagingTemplate.convertAndSendToUser(member_phone, "/queue/chat", conversationResponse);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        return messageRequest;
    }

    @DeleteMapping("user/delete-conversation/{conversationId}/{phoneDelete}")
    public String deleteConversation(@PathVariable String conversationId, @PathVariable String phoneDelete) throws ExecutionException, InterruptedException {
        conversationService.deleteConversation(conversationId, phoneDelete);
        return conversationId;
    }

    @DeleteMapping("user/delete-message/{conversationId}/{messageId}/{phoneDelete}")
    public String deleteMessage(@PathVariable String conversationId,@PathVariable String messageId,@PathVariable String phoneDelete) throws ExecutionException, InterruptedException {
        chatService.deleteMessage(conversationId, messageId, phoneDelete);
        return messageId;
    }

    @PostMapping("user/add-member/{keyPhone}/{memPhone}/{conversationId}")
    public ResponseEntity addMemberToChatGroup(@PathVariable String keyPhone, @PathVariable String memPhone, @PathVariable String conversationId) throws ExecutionException, InterruptedException {
        ConversationResponse conversationResponse = chatService.addMemberToGroupChat(conversationId, memPhone, keyPhone);
        if(conversationResponse == null) {
            return new ResponseEntity<>("Không phải là trưởng nhóm nên không có quyền quản lí nhóm", HttpStatus.UNAUTHORIZED);
        }
        conversationResponse.getConversation().getMembers().forEach(member -> {
            messagingTemplate.convertAndSendToUser(member, "queue/notify-groupchat", conversationResponse);
        });
        return new ResponseEntity(conversationResponse, HttpStatus.OK);
    }

    @PostMapping("user/delete-member/{keyPhone}/{memPhone}/{conversationId}")
    public ResponseEntity deleteMemberFromChatGroup(@PathVariable String keyPhone, @PathVariable String memPhone, @PathVariable String conversationId) throws ExecutionException, InterruptedException {
        ConversationResponse conversationResponse = chatService.deleteMemberFromGroupChat(conversationId, memPhone, keyPhone);
        if(conversationResponse == null) {
            return new ResponseEntity<>("Không phải là trưởng nhóm nên không có quyền quản lí nhóm", HttpStatus.UNAUTHORIZED);
        }
        conversationResponse.getConversation().getMembers().forEach(member -> {
            messagingTemplate.convertAndSendToUser(member, "queue/notify-groupchat", conversationResponse);
        });
        return new ResponseEntity(conversationResponse, HttpStatus.OK);
    }
}
