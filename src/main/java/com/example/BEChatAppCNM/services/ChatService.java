package com.example.BEChatAppCNM.services;

import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.entities.dto.MessageRequest;

import java.util.concurrent.ExecutionException;

public interface ChatService {
    public void saveMessage(MessageRequest messageRequest) throws ExecutionException, InterruptedException;

    public void deleteMessage(String conversationId, String messageId, String phoneDelete) throws ExecutionException, InterruptedException;
}
