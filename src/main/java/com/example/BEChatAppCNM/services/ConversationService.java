package com.example.BEChatAppCNM.services;

import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.entities.dto.ConversationResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ConversationService {
    public Conversation addConversation (Conversation conversation);

    public List<ConversationResponse> findListConversationByCreatorPhone(String creator_phone) throws ExecutionException, InterruptedException;
}
