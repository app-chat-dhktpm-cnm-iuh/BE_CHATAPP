package com.example.BEChatAppCNM.entities.dto;

import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.entities.User;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ConversationResponse {
    private Conversation conversation;
    private List<User> memberDetails;

    public Date getConversationUpdateAt() {
        return conversation.getUpdated_at();
    }
}
