package com.example.BEChatAppCNM.controllers;

import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.entities.Message;
import com.example.BEChatAppCNM.entities.User;
import com.example.BEChatAppCNM.entities.dto.ConversationResponse;
import com.example.BEChatAppCNM.entities.dto.ManageConversationResponse;
import com.example.BEChatAppCNM.entities.dto.MessageRequest;
import com.example.BEChatAppCNM.services.ChatService;
import com.example.BEChatAppCNM.services.ConversationService;
import com.example.BEChatAppCNM.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ConversationService conversationService;

    private final ChatService chatService;

    private final UserService userService;

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/user.createGroupChat")
    public ConversationResponse createGroupChat(Conversation conversation) {
        ConversationResponse conversationResult = conversationService.addConversation(conversation);

        conversation.getMembers().forEach((memberPhone) -> {
            messagingTemplate.convertAndSendToUser(memberPhone, "/queue/chat", conversationResult);
        });

        return conversationResult;
    }

    @PostMapping("user/groupchat/update")
    public ConversationResponse updateGroupChatDetail(@RequestBody Conversation conversation) throws ExecutionException, InterruptedException {
        conversationService.updateGroupChatDetail(conversation);

        TimeUnit.SECONDS.sleep(2);

        ConversationResponse conversationResponse = conversationService.getConversationById(conversation.getConversation_id());

        conversationResponse.getConversation().getMembers().forEach((memberPhone) -> {
            messagingTemplate.convertAndSendToUser(memberPhone, "queue/notifyGroupchat", conversationResponse);
        });

        return  conversationResponse;
    }

    @CrossOrigin("http://localhost:5173")
    @GetMapping("user/messages/{creator_phone}")
    public ResponseEntity findListConversation(@PathVariable String creator_phone) throws ExecutionException, InterruptedException {
        List<ConversationResponse> conversationList = conversationService.findListConversationByCreatorPhone(creator_phone);
        if(!conversationList.isEmpty()) {
            return new ResponseEntity<>(conversationList, HttpStatus.OK);
        } else return new ResponseEntity<>("Không tìm thấy hội thoại nào", HttpStatus.NOT_FOUND);
    }

    @CrossOrigin("http://localhost:5173")
    @GetMapping("user/message/{currentPhone}/{userPhone}")
    public ResponseEntity findConversationByCurrentPhoneAndUserPhone(@PathVariable String currentPhone, @PathVariable String userPhone) throws ExecutionException, InterruptedException {
        ConversationResponse conversationResponse = conversationService.getConversationBySenderPhoneAndReceiverPhone(currentPhone, userPhone);

            if(conversationResponse.getConversation() != null) {
            return new ResponseEntity<>(conversationResponse, HttpStatus.OK);
        } return new ResponseEntity<>("Không tìm thấy", HttpStatus.NOT_FOUND);
    }

    @CrossOrigin("http://localhost:5173")
    @GetMapping("user/conversations/{conversation_id}/{currentPhone}")
    public ResponseEntity findConversationByIDAndCurrentPhone(@PathVariable String conversation_id, @PathVariable String currentPhone) throws ExecutionException, InterruptedException {
        ConversationResponse conversation = conversationService.getConversationByIdAndCurrentPhone(conversation_id, currentPhone);
        if(conversation != null) {
            return new ResponseEntity<>(conversation, HttpStatus.OK);
        } else return new ResponseEntity<>("Không tìm thấy hội thoại nào", HttpStatus.NOT_FOUND);
    }

    @MessageMapping("/chat")
    public MessageRequest saveMessage(MessageRequest messageRequest) throws ExecutionException, InterruptedException {
        Message message = chatService.saveMessage(messageRequest);

        MessageRequest messageReturn = MessageRequest.builder()
                .conversation_id(messageRequest.getConversation_id())
                .message_id(message.getMessage_id())
                .sender_name(message.getSender_name())
                .sender_phone(message.getSender_phone())
                .is_read(message.is_read())
                .members(messageRequest.getMembers())
                .images(message.getImages())
                .attaches(message.getAttaches())
                .content(message.getContent())
                .sender_avatar_url(message.getSender_avatar_url())
                .phoneDeleteList(message.getPhoneDeleteList())
                .sent_date_time(message.getSent_date_time())
                .build();

        messageRequest.getMembers().forEach(member_phone -> {
            messagingTemplate.convertAndSendToUser(member_phone, "/queue/messages", messageReturn);
            try {

                ConversationResponse conversationResponse = conversationService.getConversationByIdAndCurrentPhone(messageRequest.getConversation_id(), member_phone);

                if(conversationResponse.getConversation().getMessages().isEmpty()) {
                    List<Message> messages = new ArrayList<>();
                    messages.add(message);
                    conversationResponse.getConversation().setMessages(messages);
                    messagingTemplate.convertAndSendToUser(member_phone, "/queue/chat", conversationResponse);
                } else {
                    messagingTemplate.convertAndSendToUser(member_phone, "/queue/chat", conversationResponse);
                }
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

        ManageConversationResponse manageConversationResponse = ManageConversationResponse
                .builder()
                .conversationId(conversationId)
                .is_deleted(false)
                .build();

        List<String> members = conversationResponse.getConversation().getMembers();

        List<Message> messages = conversationResponse.getConversation().getMessages();

        Message message = messages.get(messages.size() - 1);

        MessageRequest messageReturn = MessageRequest.builder()
                .conversation_id(conversationId)
                .message_id(message.getMessage_id())
                .sender_name(message.getSender_name())
                .sender_phone(message.getSender_phone())
                .is_read(message.is_read())
                .members(conversationResponse.getConversation().getMembers())
                .images(message.getImages())
                .attaches(message.getAttaches())
                .content(message.getContent())
                .sender_avatar_url(message.getSender_avatar_url())
                .phoneDeleteList(message.getPhoneDeleteList())
                .sent_date_time(message.getSent_date_time())
                .build();

        members.forEach(member -> {
            messagingTemplate.convertAndSendToUser(member, "queue/notifyGroupchat", manageConversationResponse);
        });

        members.forEach(member_phone -> {
            messagingTemplate.convertAndSendToUser(member_phone, "/queue/messages", messageReturn);
        });

        return new ResponseEntity(manageConversationResponse, HttpStatus.OK);
    }

    @PostMapping("user/delete-member/{keyPhone}/{memPhone}/{conversationId}")
    public ResponseEntity deleteMemberFromChatGroup(@PathVariable String keyPhone, @PathVariable String memPhone, @PathVariable String conversationId) throws ExecutionException, InterruptedException {
        ConversationResponse conversationResponse = chatService.deleteMemberFromGroupChat(conversationId, memPhone, keyPhone);
        if(conversationResponse == null) {
            return new ResponseEntity<>("Không phải là trưởng nhóm nên không có quyền quản lí nhóm", HttpStatus.UNAUTHORIZED);
        }
        User deletedUser = userService.getUserDetailsByPhone(memPhone).get();

        List<String> members = conversationResponse.getConversation().getMembers();

        ManageConversationResponse manageConversationResponse = ManageConversationResponse
                .builder()
                .conversationId(conversationId)
                .deletedUser(deletedUser)
                .is_deleted(true)
                .build();

        List<Message> messages = conversationResponse.getConversation().getMessages();

        Message message = messages.get(messages.size() - 1);

        MessageRequest messageReturn = MessageRequest.builder()
                .conversation_id(conversationId)
                .message_id(message.getMessage_id())
                .sender_name(message.getSender_name())
                .sender_phone(message.getSender_phone())
                .is_read(message.is_read())
                .members(conversationResponse.getConversation().getMembers())
                .images(message.getImages())
                .attaches(message.getAttaches())
                .content(message.getContent())
                .sender_avatar_url(message.getSender_avatar_url())
                .phoneDeleteList(message.getPhoneDeleteList())
                .sent_date_time(message.getSent_date_time())
                .build();

        members.forEach(member_phone -> {
            messagingTemplate.convertAndSendToUser(member_phone, "/queue/messages", messageReturn);
        });

        members.forEach(member -> {
            messagingTemplate.convertAndSendToUser(member, "queue/notifyGroupchat", manageConversationResponse);
        });

        messagingTemplate.convertAndSendToUser(memPhone, "queue/notifyGroupchat", manageConversationResponse);

        return new ResponseEntity(conversationResponse, HttpStatus.OK);
    }

    @DeleteMapping("user/disband-groupchat/{keyPhone}/{conversationId}")
    public ResponseEntity disbandTheChatGroup(@PathVariable String keyPhone, @PathVariable String conversationId) throws ExecutionException, InterruptedException {
        Conversation conversationResponse = chatService.disbandGroupChat(conversationId, keyPhone);
        if(conversationResponse == null) {
            return new ResponseEntity<>("Không phải là trưởng nhóm nên không có quyền quản lí nhóm", HttpStatus.UNAUTHORIZED);
        }

        ManageConversationResponse manageConversationResponse = ManageConversationResponse
                .builder()
                .conversationId(conversationId)
                .is_deleted(true)
                .build();

        conversationResponse.getMembers().forEach(member -> {
            messagingTemplate.convertAndSendToUser(member, "queue/notifyGroupchat", manageConversationResponse);
        });
        return new ResponseEntity(conversationResponse, HttpStatus.OK);
    }
}
