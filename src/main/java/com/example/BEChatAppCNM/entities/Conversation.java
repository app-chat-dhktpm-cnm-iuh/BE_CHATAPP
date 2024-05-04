package com.example.BEChatAppCNM.entities;

import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Conversation {
    private String conversation_id;
    private String title;
    private String creator_phone;
    private String ava_conversation_url;
    private List<DeleteConversationUser> deleteConversationUsers;
    private Date updated_at;
    private List<String> members;
    private List<Message> messages;
}
