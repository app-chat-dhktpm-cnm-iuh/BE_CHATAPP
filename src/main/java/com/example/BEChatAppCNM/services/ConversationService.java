package com.example.BEChatAppCNM.services;

import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.entities.dto.ConversationResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ConversationService {
    public ConversationResponse addConversation (Conversation conversation);

    public List<ConversationResponse> findListConversationByCreatorPhone(String creator_phone) throws ExecutionException, InterruptedException;
    public ConversationResponse getConversationById(String conversationId) throws ExecutionException, InterruptedException;
    public ConversationResponse getConversationByIdAndCurrentPhone(String conversationId, String currentPhone) throws ExecutionException, InterruptedException;

    public ConversationResponse getConversationBySenderPhoneAndReceiverPhone(String currentPhone, String userPhone) throws ExecutionException, InterruptedException;

    public void deleteConversation(String conversationId, String currentPhone) throws ExecutionException, InterruptedException;
}
