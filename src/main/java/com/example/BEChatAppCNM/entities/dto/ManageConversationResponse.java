package com.example.BEChatAppCNM.entities.dto;

import com.example.BEChatAppCNM.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManageConversationResponse {
    private User deletedUser;
    private boolean is_deleted;
    private String conversationId;
}
