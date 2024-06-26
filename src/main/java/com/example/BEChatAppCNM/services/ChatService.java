package com.example.BEChatAppCNM.services;

import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.entities.Message;
import com.example.BEChatAppCNM.entities.dto.ConversationResponse;
import com.example.BEChatAppCNM.entities.dto.MessageRequest;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ChatService {
    public Message saveMessage(MessageRequest messageRequest) throws ExecutionException, InterruptedException;

    public void deleteMessage(String conversationId, String messageId, String phoneDelete) throws ExecutionException, InterruptedException;

    public ConversationResponse addMemberToGroupChat(String conversationId, List<String> memPhoneList, String keyPhone) throws ExecutionException, InterruptedException;

    public ConversationResponse deleteMemberFromGroupChat(String conversationId, String memPhone, String keyPhone) throws ExecutionException, InterruptedException;

    public Conversation disbandGroupChat(String conversationId, String keyPhone) throws ExecutionException, InterruptedException;
}
