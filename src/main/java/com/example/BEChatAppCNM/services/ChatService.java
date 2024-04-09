package com.example.BEChatAppCNM.services;

import com.example.BEChatAppCNM.entities.dto.MessageRequest;

public interface ChatService {
    public void saveMessage(MessageRequest messageRequest);
}
